package no.nav.sosialhjelp.soknad.ettersending

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.exceptions.EttersendelseSendtForSentException
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.isVedleggskravAnnet
import no.nav.sosialhjelp.soknad.innsending.SenderUtils
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.*

@Deprecated("SvarUt og denne type ettersending støttes ikke lenger")
@Component
class EttersendingService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val clock: Clock
) {
    @Deprecated("SvarUt og denne type ettersending støttes ikke lenger")
    fun startEttersendelse(behandlingsIdDetEttersendesPaa: String?): String {
        val originalSoknad = hentOgVerifiserSoknad(behandlingsIdDetEttersendesPaa)
        val nyesteSoknad = hentNyesteSoknadIKjede(originalSoknad)

        val nyBehandlingsId = opprettSoknadMetadataEttersendelse(originalSoknad)
        MdcOperations.putToMDC(MdcOperations.MDC_BEHANDLINGS_ID, nyBehandlingsId)

        val manglendeVedlegg = lagListeOverVedlegg(nyesteSoknad)
        val manglendeJsonVedlegg = convertVedleggMetadataToJsonVedlegg(manglendeVedlegg)

        lagreSoknadILokalDb(originalSoknad, nyBehandlingsId, manglendeJsonVedlegg)

        return nyBehandlingsId
    }

    private fun opprettSoknadMetadataEttersendelse(ettersendesPaSoknad: SoknadMetadata): String {
        val nextId = soknadMetadataRepository.hentNesteId()

        val ettersendelse = SoknadMetadata(
            id = 0,
            behandlingsId = UUID.randomUUID().toString(),
            idGammeltFormat = SenderUtils.lagBehandlingsId(nextId),
            tilknyttetBehandlingsId = ettersendesPaSoknad.behandlingsId,
            fnr = ettersendesPaSoknad.fnr,
            skjema = ettersendesPaSoknad.skjema,
            orgnr = ettersendesPaSoknad.orgnr,
            navEnhet = ettersendesPaSoknad.navEnhet,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING,
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(clock),
            sistEndretDato = LocalDateTime.now(clock)
        )
        soknadMetadataRepository.opprett(ettersendelse)
        return ettersendelse.behandlingsId
    }

    private fun lagreSoknadILokalDb(
        originalSoknad: SoknadMetadata,
        nyBehandlingsId: String,
        manglendeJsonVedlegg: List<JsonVedlegg>
    ) {
        val ettersendingSoknad = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = nyBehandlingsId,
            tilknyttetBehandlingsId = originalSoknad.behandlingsId,
            eier = originalSoknad.fnr,
            jsonInternalSoknad = JsonInternalSoknad()
                .withVedlegg(
                    JsonVedleggSpesifikasjon()
                        .withVedlegg(manglendeJsonVedlegg)
                )
                .withMottaker(
                    JsonSoknadsmottaker()
                        .withOrganisasjonsnummer(originalSoknad.orgnr)
                        .withNavEnhetsnavn(originalSoknad.navEnhet)
                ),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

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

    private fun hentOgVerifiserSoknad(behandlingsId: String?): SoknadMetadata {
        var soknad = soknadMetadataRepository.hent(behandlingsId)
            ?: throw IllegalStateException("SoknadMetadata til behandlingsid $behandlingsId finnes ikke")

        if (soknad.type == SoknadMetadataType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            soknadMetadataRepository.hent(soknad.tilknyttetBehandlingsId)?.let { soknad = it }
        }

        if (soknad.status != FERDIG) {
            throw SosialhjelpSoknadApiException("Kan ikke starte ettersendelse på noe som ikke er innsendt")
        } else if (soknad.innsendtDato?.isBefore(LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER.toLong())) == true) {
            throwDetailedExceptionForEttersendelserEtterFrist(soknad)
        }
        return soknad
    }

    private fun throwDetailedExceptionForEttersendelserEtterFrist(soknad: SoknadMetadata) {
        val dagerEtterFrist = DAYS.between(
            soknad.innsendtDato,
            LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER.toLong())
        )
        val frist = soknad.innsendtDato?.plusDays(ETTERSENDELSE_FRIST_DAGER.toLong())
        val dateTimeFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy HH:mm:ss")
        val antallEttersendelser = hentAntallEttersendelserSendtPaSoknad(soknad.behandlingsId)
        val antallNyereSoknader = hentAntallInnsendteSoknaderEtterTidspunkt(soknad.fnr, soknad.innsendtDato).toLong()
        throw EttersendelseSendtForSentException(
            "Kan ikke starte ettersendelse $dagerEtterFrist dager etter frist, " +
                "dagens dato: ${LocalDateTime.now().format(dateTimeFormatter)}, " +
                "soknadens dato: ${soknad.innsendtDato?.format(dateTimeFormatter)}, " +
                "frist($ETTERSENDELSE_FRIST_DAGER dager): ${frist?.format(dateTimeFormatter)}. " +
                "Antall ettersendelser som er sendt på denne søknaden tidligere er: $antallEttersendelser. " +
                "Antall nyere søknader denne brukeren har: $antallNyereSoknader"
        )
    }

    private fun hentNyesteSoknadIKjede(originalSoknad: SoknadMetadata): SoknadMetadata {
        return soknadMetadataRepository.hentBehandlingskjede(originalSoknad.behandlingsId)
            .filter { it.status == FERDIG }
            .maxByOrNull { it.innsendtDato ?: LocalDateTime.MIN }
            ?: originalSoknad
    }

    private fun hentAntallEttersendelserSendtPaSoknad(behandlingsId: String?): Long {
        return soknadMetadataRepository.hentBehandlingskjede(behandlingsId)
            .count { it.status == FERDIG }.toLong()
    }

    private fun lagListeOverVedlegg(nyesteSoknad: SoknadMetadata): List<VedleggMetadata> {
        val manglendeVedlegg = nyesteSoknad.vedlegg?.vedleggListe
            ?.filter { it.status == Vedleggstatus.VedleggKreves }
            ?.toMutableList() ?: mutableListOf()

        if (manglendeVedlegg.none { isVedleggskravAnnet(it) }) {
            val annetVedlegg = VedleggMetadata(
                skjema = "annet",
                tillegg = "annet",
                hendelseType = JsonVedlegg.HendelseType.BRUKER
            )
            manglendeVedlegg.add(annetVedlegg)
        }
        return manglendeVedlegg
    }

    private fun hentAntallInnsendteSoknaderEtterTidspunkt(fnr: String?, tidspunkt: LocalDateTime?): Int {
        return soknadMetadataRepository.hentAntallInnsendteSoknaderEtterTidspunkt(fnr, tidspunkt) ?: 0
    }

    companion object {
        private const val ETTERSENDELSE_FRIST_DAGER = 90
    }
}
