package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.SKJEMANUMMER
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.lagBehandlingsId
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteSystemdata
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenSystemdata
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.scheduled.hasMellomlagredeVedlegg
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Component
class SoknadService(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val systemdataUpdater: SystemdataUpdater,
    private val bostotteSystemdata: BostotteSystemdata,
    private val skatteetatenSystemdata: SkatteetatenSystemdata,
    private val mellomlagringService: MellomlagringService,
    private val prometheusMetricsService: PrometheusMetricsService,
    private val clock: Clock
) {
    @Transactional
    fun startSoknad(): String {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val behandlingsId = opprettSoknadMetadata(eier)
        MdcOperations.putToMDC(MdcOperations.MDC_BEHANDLINGS_ID, behandlingsId)

        prometheusMetricsService.reportStartSoknad()

        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            eier = eier,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        systemdataUpdater.update(soknadUnderArbeid)
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, eier)

        return behandlingsId
    }

    private fun opprettSoknadMetadata(fnr: String): String {
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
        return soknadMetadata.behandlingsId.also {
            log.info("Starter søknad $it")
        }
    }

    fun oppdaterSistEndretDatoPaaMetadata(behandlingsId: String?) {
        val hentet = soknadMetadataRepository.hent(behandlingsId)
        hentet?.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(hentet)
    }

    @Transactional
    fun avbrytSoknad(behandlingsId: String, steg: String) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        soknadUnderArbeidRepository.hentSoknadNullable(behandlingsId, eier)?.let {

            if (it.hasMellomlagredeVedlegg()) {
                mellomlagringService.deleteAllVedlegg(it.behandlingsId)
            }

            soknadUnderArbeidRepository.slettSoknad(it, eier)
            settSoknadMetadataAvbrutt(it.behandlingsId, false)

            prometheusMetricsService.reportAvbruttSoknad(steg)
        }
    }

    fun settSoknadMetadataAvbrutt(behandlingsId: String?, avbruttAutomatisk: Boolean) {
        val metadata = soknadMetadataRepository.hent(behandlingsId)
        metadata?.status = if (avbruttAutomatisk) SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK else SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER
        metadata?.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(metadata)
    }

    @Transactional
    fun oppdaterSamtykker(
        behandlingsId: String?,
        harBostotteSamtykke: Boolean,
        harSkatteetatenSamtykke: Boolean,
        token: String?,
    ) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        if (harSkatteetatenSamtykke) {
            skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)
        }
        if (harBostotteSamtykke) {
            bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, token)
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadService::class.java)

        fun createEmptyJsonInternalSoknad(eier: String): JsonInternalSoknad {
            return JsonInternalSoknad().withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withPersonalia(
                                JsonPersonalia()
                                    .withPersonIdentifikator(
                                        JsonPersonIdentifikator()
                                            .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                            .withVerdi(eier)
                                    )
                                    .withNavn(
                                        JsonSokernavn()
                                            .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                            .withFornavn("")
                                            .withMellomnavn("")
                                            .withEtternavn("")
                                    )
                                    .withKontonummer(
                                        JsonKontonummer()
                                            .withKilde(JsonKilde.SYSTEM)
                                    )
                            )
                            .withArbeid(JsonArbeid())
                            .withUtdanning(
                                JsonUtdanning()
                                    .withKilde(JsonKilde.BRUKER)
                            )
                            .withFamilie(
                                JsonFamilie()
                                    .withForsorgerplikt(JsonForsorgerplikt())
                            )
                            .withBegrunnelse(
                                JsonBegrunnelse()
                                    .withKilde(JsonKildeBruker.BRUKER)
                                    .withHvorforSoke("")
                                    .withHvaSokesOm("")
                            )
                            .withBosituasjon(
                                JsonBosituasjon()
                                    .withKilde(JsonKildeBruker.BRUKER)
                            )
                            .withOkonomi(
                                JsonOkonomi()
                                    .withOpplysninger(
                                        JsonOkonomiopplysninger()
                                            .withUtbetaling(ArrayList())
                                            .withUtgift(ArrayList())
                                            .withBostotte(JsonBostotte())
                                            .withBekreftelse(ArrayList())
                                    )
                                    .withOversikt(
                                        JsonOkonomioversikt()
                                            .withInntekt(ArrayList())
                                            .withUtgift(ArrayList())
                                            .withFormue(ArrayList())
                                    )
                            )
                    )
                    .withMottaker(
                        JsonSoknadsmottaker()
                            .withNavEnhetsnavn("")
                            .withEnhetsnummer("")
                    )
                    .withDriftsinformasjon(
                        JsonDriftsinformasjon()
                            .withUtbetalingerFraNavFeilet(false)
                            .withInntektFraSkatteetatenFeilet(false)
                            .withStotteFraHusbankenFeilet(false)
                    )
                    .withKompatibilitet(ArrayList())
            ).withVedlegg(JsonVedleggSpesifikasjon())
        }
    }
}
