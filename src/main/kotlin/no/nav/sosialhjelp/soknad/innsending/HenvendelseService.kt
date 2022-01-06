package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalDateTime
import java.util.Locale

class HenvendelseService(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val clock: Clock
) {
    fun startSoknad(fnr: String?): String {
        logger.info("Starter søknad")
        val meta = SoknadMetadata()
        meta.id = soknadMetadataRepository.hentNesteId()
        meta.behandlingsId = lagBehandlingsId(meta.id)
        meta.fnr = fnr
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL
        meta.skjema = SosialhjelpInformasjon.SKJEMANUMMER
        meta.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID
        meta.opprettetDato = LocalDateTime.now(clock)
        meta.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.opprett(meta)
        return meta.behandlingsId
    }

    fun startEttersending(ettersendesPaSoknad: SoknadMetadata?): String {
        val ettersendelse = SoknadMetadata()
        ettersendelse.id = soknadMetadataRepository.hentNesteId()
        ettersendelse.behandlingsId = lagBehandlingsId(ettersendelse.id)
        ettersendelse.tilknyttetBehandlingsId = ettersendesPaSoknad?.behandlingsId
        ettersendelse.fnr = ettersendesPaSoknad?.fnr
        ettersendelse.type = SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING
        ettersendelse.skjema = ettersendesPaSoknad?.skjema
        ettersendelse.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID
        ettersendelse.opprettetDato = LocalDateTime.now(clock)
        ettersendelse.sistEndretDato = LocalDateTime.now(clock)
        ettersendelse.orgnr = ettersendesPaSoknad?.orgnr
        ettersendelse.navEnhet = ettersendesPaSoknad?.navEnhet
        soknadMetadataRepository.opprett(ettersendelse)
        return ettersendelse.behandlingsId
    }

    fun hentBehandlingskjede(behandlingskjedeId: String?): List<SoknadMetadata> {
        return soknadMetadataRepository.hentBehandlingskjede(behandlingskjedeId)
    }

    fun hentAntallInnsendteSoknaderEtterTidspunkt(fnr: String?, tidspunkt: LocalDateTime?): Int {
        return soknadMetadataRepository.hentAntallInnsendteSoknaderEtterTidspunkt(fnr, tidspunkt)
    }

    fun oppdaterMetadataVedAvslutningAvSoknad(
        behandlingsId: String?,
        vedlegg: VedleggMetadataListe,
        soknadUnderArbeid: SoknadUnderArbeid,
        brukerDigisosApi: Boolean
    ) {
        val meta = soknadMetadataRepository.hent(behandlingsId)
        meta.vedlegg = vedlegg
        if (meta.type != SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            meta.orgnr = soknadUnderArbeid.jsonInternalSoknad.mottaker.organisasjonsnummer
            meta.navEnhet = soknadUnderArbeid.jsonInternalSoknad.mottaker.navEnhetsnavn
        }
        meta.sistEndretDato = LocalDateTime.now(clock)
        meta.innsendtDato = LocalDateTime.now(clock)
        meta.status = if (brukerDigisosApi) SENDT_MED_DIGISOS_API else FERDIG
        soknadMetadataRepository.oppdater(meta)
        logger.info("Søknad avsluttet $behandlingsId ${meta.skjema}, ${vedlegg.vedleggListe.size}")
    }

    fun hentSoknad(behandlingsId: String?): SoknadMetadata {
        return soknadMetadataRepository.hent(behandlingsId)
    }

    fun oppdaterSistEndretDatoPaaMetadata(behandlingsId: String?) {
        val hentet = soknadMetadataRepository.hent(behandlingsId)
        hentet.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(hentet)
    }

    fun avbrytSoknad(behandlingsId: String?, avbruttAutomatisk: Boolean) {
        val metadata = soknadMetadataRepository.hent(behandlingsId)
        metadata.status = if (avbruttAutomatisk) AVBRUTT_AUTOMATISK else AVBRUTT_AV_BRUKER
        metadata.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(metadata)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HenvendelseService::class.java)
        fun lagBehandlingsId(databasenokkel: Long): String {
            val applikasjonsprefix = "11"
            val base = (applikasjonsprefix + "0000000").toLong(36)
            val behandlingsId = (base + databasenokkel).toString(36).uppercase(Locale.getDefault())
                .replace("O", "o").replace("I", "i")
            if (!behandlingsId.startsWith(applikasjonsprefix)) {
                throw SosialhjelpSoknadApiException("Tildelt sekvensrom for behandlingsId er brukt opp. Kan ikke generer behandlingsId $behandlingsId")
            }
            MDCOperations.putToMDC(MDCOperations.MDC_BEHANDLINGS_ID, behandlingsId)
            return behandlingsId
        }
    }
}
