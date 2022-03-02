package no.nav.sosialhjelp.soknad.ettersending

import no.finn.unleash.Unleash
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.common.exceptions.EttersendelseSendtForSentException
import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.FEATURE_UTVIDE_VEDLEGGJSON
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.isVedleggskravAnnet
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS

class EttersendingService(
    private val henvendelseService: HenvendelseService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val unleash: Unleash,
    private val clock: Clock
) {
    fun start(behandlingsIdDetEttersendesPaa: String?): String {
        val originalSoknad = hentOgVerifiserSoknad(behandlingsIdDetEttersendesPaa)
        val nyesteSoknad = hentNyesteSoknadIKjede(originalSoknad)

        val nyBehandlingsId = henvendelseService.startEttersending(originalSoknad)

        val manglendeVedlegg = lagListeOverVedlegg(nyesteSoknad)
        val manglendeJsonVedlegg = convertVedleggMetadataToJsonVedlegg(manglendeVedlegg)

        lagreSoknadILokalDb(originalSoknad, nyBehandlingsId, manglendeJsonVedlegg)

        return nyBehandlingsId
    }

    private fun lagreSoknadILokalDb(
        originalSoknad: SoknadMetadata?,
        nyBehandlingsId: String,
        manglendeJsonVedlegg: List<JsonVedlegg>
    ) {
        val ettersendingSoknad = SoknadUnderArbeid()
            .withBehandlingsId(nyBehandlingsId)
            .withVersjon(1L)
            .withEier(originalSoknad!!.fnr)
            .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)
            .withTilknyttetBehandlingsId(originalSoknad.behandlingsId)
            .withJsonInternalSoknad(
                JsonInternalSoknad()
                    .withVedlegg(JsonVedleggSpesifikasjon().withVedlegg(manglendeJsonVedlegg))
                    .withMottaker(
                        JsonSoknadsmottaker()
                            .withOrganisasjonsnummer(originalSoknad.orgnr)
                            .withNavEnhetsnavn(originalSoknad.navEnhet)
                    )
            )
            .withOpprettetDato(LocalDateTime.now())
            .withSistEndretDato(LocalDateTime.now())

        soknadUnderArbeidRepository.opprettSoknad(ettersendingSoknad, originalSoknad.fnr)
    }

    private fun convertVedleggMetadataToJsonVedlegg(manglendeVedlegg: List<VedleggMetadata>): List<JsonVedlegg> {
        return manglendeVedlegg
            .map {
                JsonVedlegg()
                    .withType(it.skjema)
                    .withTilleggsinfo(it.tillegg)
                    .withStatus("VedleggKreves")
                    .withHendelseType(it.hendelseType)
                    .withHendelseReferanse(it.hendelseReferanse)
            }
    }

    private fun hentOgVerifiserSoknad(behandlingsId: String?): SoknadMetadata? {
        var soknad = henvendelseService.hentSoknad(behandlingsId)
            ?: throw IllegalStateException("SoknadMetadata til behandlingsid $behandlingsId finnes ikke")

        if (soknad.type == SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            henvendelseService.hentSoknad(soknad.tilknyttetBehandlingsId)?.let { soknad = it }
        }

        if (soknad.status != FERDIG) {
            throw SosialhjelpSoknadApiException("Kan ikke starte ettersendelse på noe som ikke er innsendt")
        } else if (soknad.innsendtDato.isBefore(LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER.toLong()))) {
            throwDetailedExceptionForEttersendelserEtterFrist(soknad)
        }
        return soknad
    }

    private fun throwDetailedExceptionForEttersendelserEtterFrist(soknad: SoknadMetadata) {
        val dagerEtterFrist = DAYS.between(
            soknad.innsendtDato,
            LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER.toLong())
        )
        val dateTimeFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy HH:mm:ss")
        val antallEttersendelser = hentAntallEttersendelserSendtPaSoknad(soknad.behandlingsId)
        val antallNyereSoknader = henvendelseService.hentAntallInnsendteSoknaderEtterTidspunkt(soknad.fnr, soknad.innsendtDato).toLong()
        throw EttersendelseSendtForSentException(
            "Kan ikke starte ettersendelse $dagerEtterFrist dager etter frist, " +
                "dagens dato: ${LocalDateTime.now().format(dateTimeFormatter)}, " +
                "soknadens dato: ${soknad.innsendtDato.format(dateTimeFormatter)}, " +
                "frist($ETTERSENDELSE_FRIST_DAGER dager): ${LocalDateTime.now().minusDays(ETTERSENDELSE_FRIST_DAGER.toLong()).format(dateTimeFormatter)}. " +
                "Antall ettersendelser som er sendt på denne søknaden tidligere er: $antallEttersendelser. " +
                "Antall nyere søknader denne brukeren har: $antallNyereSoknader",
        )
    }

    fun hentNyesteSoknadIKjede(originalSoknad: SoknadMetadata?): SoknadMetadata {
        return henvendelseService.hentBehandlingskjede(originalSoknad!!.behandlingsId)
            .filter { it.status == FERDIG }
            .maxByOrNull { it.innsendtDato }
            ?: originalSoknad
    }

    private fun hentAntallEttersendelserSendtPaSoknad(behandlingsId: String?): Long {
        return henvendelseService.hentBehandlingskjede(behandlingsId)
            .count { it.status == FERDIG }.toLong()
    }

    private fun lagListeOverVedlegg(nyesteSoknad: SoknadMetadata): List<VedleggMetadata> {
        val manglendeVedlegg = nyesteSoknad.vedlegg.vedleggListe
            .filter { it.status == Vedleggstatus.VedleggKreves }
            .toMutableList()

        if (manglendeVedlegg.none { isVedleggskravAnnet(it) }) {
            val annetVedlegg = VedleggMetadata()
            annetVedlegg.skjema = "annet"
            annetVedlegg.tillegg = "annet"
            if (unleash.isEnabled(FEATURE_UTVIDE_VEDLEGGJSON, false)) {
                annetVedlegg.hendelseType = JsonVedlegg.HendelseType.BRUKER
            }
            manglendeVedlegg.add(annetVedlegg)
        }
        return manglendeVedlegg
    }

    companion object {
        const val ETTERSENDELSE_FRIST_DAGER = 300
    }
}
