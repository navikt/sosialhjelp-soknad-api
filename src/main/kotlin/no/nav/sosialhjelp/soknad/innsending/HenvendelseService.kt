package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.util.Locale

@Component
class HenvendelseService(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val clock: Clock
) {
    fun startSoknad(fnr: String): String {
        logger.info("Starter søknad")
        val id = soknadMetadataRepository.hentNesteId()
        val soknadMetadata = SoknadMetadata(
            id = id,
            behandlingsId = lagBehandlingsId(id),
            fnr = fnr,
            skjema = SKJEMANUMMER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(clock),
            sistEndretDato = LocalDateTime.now(clock)
        )
        soknadMetadataRepository.opprett(soknadMetadata)
        return soknadMetadata.behandlingsId
    }

    fun startEttersending(ettersendesPaSoknad: SoknadMetadata): String {
        val id = soknadMetadataRepository.hentNesteId()
        val ettersendelse = SoknadMetadata(
            id = id,
            behandlingsId = lagBehandlingsId(id),
            tilknyttetBehandlingsId = ettersendesPaSoknad.behandlingsId,
            fnr = ettersendesPaSoknad.fnr,
            skjema = ettersendesPaSoknad.skjema,
            orgnr = ettersendesPaSoknad.orgnr,
            navEnhet = ettersendesPaSoknad.navEnhet,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING,
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(clock),
            sistEndretDato = LocalDateTime.now(clock),
        )
        soknadMetadataRepository.opprett(ettersendelse)
        return ettersendelse.behandlingsId
    }

    fun hentBehandlingskjede(behandlingskjedeId: String?): List<SoknadMetadata> {
        return soknadMetadataRepository.hentBehandlingskjede(behandlingskjedeId)
    }

    fun hentAntallInnsendteSoknaderEtterTidspunkt(fnr: String?, tidspunkt: LocalDateTime?): Int {
        return soknadMetadataRepository.hentAntallInnsendteSoknaderEtterTidspunkt(fnr, tidspunkt) ?: 0
    }

    fun oppdaterMetadataVedAvslutningAvSoknad(
        behandlingsId: String?,
        vedlegg: VedleggMetadataListe,
        soknadUnderArbeid: SoknadUnderArbeid,
        brukerDigisosApi: Boolean
    ) {
        val meta = soknadMetadataRepository.hent(behandlingsId)
        meta?.vedlegg = vedlegg
        if (meta?.type != SoknadMetadataType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            meta?.orgnr = soknadUnderArbeid.jsonInternalSoknad?.mottaker?.organisasjonsnummer
            meta?.navEnhet = soknadUnderArbeid.jsonInternalSoknad?.mottaker?.navEnhetsnavn
        }
        meta?.sistEndretDato = LocalDateTime.now(clock)
        meta?.innsendtDato = LocalDateTime.now(clock)
        meta?.status = if (brukerDigisosApi) SENDT_MED_DIGISOS_API else FERDIG
        soknadMetadataRepository.oppdater(meta)
        logger.info("Søknad avsluttet $behandlingsId ${meta?.skjema}, ${vedlegg.vedleggListe.size}")
    }

    fun hentSoknad(behandlingsId: String?): SoknadMetadata? {
        return soknadMetadataRepository.hent(behandlingsId)
    }

    fun oppdaterSistEndretDatoPaaMetadata(behandlingsId: String?) {
        val hentet = soknadMetadataRepository.hent(behandlingsId)
        hentet?.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(hentet)
    }

    fun avbrytSoknad(behandlingsId: String?, avbruttAutomatisk: Boolean) {
        val metadata = soknadMetadataRepository.hent(behandlingsId)
        metadata?.status = if (avbruttAutomatisk) AVBRUTT_AUTOMATISK else AVBRUTT_AV_BRUKER
        metadata?.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(metadata)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HenvendelseService::class.java)

        const val SKJEMANUMMER = "NAV 35-18.01"

        fun lagBehandlingsId(databasenokkel: Long): String {
            val applikasjonsprefix = "11"
            val base = (applikasjonsprefix + "0000000").toLong(36)
            val behandlingsId = (base + databasenokkel).toString(36).uppercase(Locale.getDefault())
                .replace("O", "o").replace("I", "i")
            if (!behandlingsId.startsWith(applikasjonsprefix)) {
                throw SosialhjelpSoknadApiException("Tildelt sekvensrom for behandlingsId er brukt opp. Kan ikke generer behandlingsId $behandlingsId")
            }
            MdcOperations.putToMDC(MdcOperations.MDC_BEHANDLINGS_ID, behandlingsId)
            return behandlingsId
        }
    }
}
