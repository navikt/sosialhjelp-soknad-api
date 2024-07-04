package no.nav.sosialhjelp.soknad.innsending

import io.getunleash.Unleash
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
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
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
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteSystemdata
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenSystemdata
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.ArrayList

@Component
class SoknadServiceOld(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val systemdataUpdater: SystemdataUpdater,
    private val bostotteSystemdata: BostotteSystemdata,
    private val skatteetatenSystemdata: SkatteetatenSystemdata,
    private val mellomlagringService: MellomlagringService,
    private val prometheusMetricsService: PrometheusMetricsService,
    private val clock: Clock,
    private val v2AdapterService: V2AdapterService,
    private val unleash: Unleash,
    private val digisosApiService: DigisosApiService,
) {
    @Transactional
    fun startSoknad(
        token: String?,
        type: JsonData.Soknadstype?,
    ): StartSoknadResponse {
        val eierId = SubjectHandlerUtils.getUserIdFromToken()

        val kortSoknad = (type == null || type == JsonData.Soknadstype.KORT) && isKortSoknadEnabled() && qualifiesForKortSoknad(eierId, token)

        val behandlingsId = opprettSoknadMetadata(eierId, kortSoknad = kortSoknad) // TODO NyModell Metadata returnerer UUID

        MdcOperations.putToMDC(MdcOperations.MDC_BEHANDLINGS_ID, behandlingsId)
        if (kortSoknad) {
            log.info("Starter kort søknad")
        } else {
            log.info("Starter søknad")
        }
        prometheusMetricsService.reportStartSoknad(kortSoknad)

        val soknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = behandlingsId,
                eier = eierId,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(eierId, kortSoknad),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )

        // ny modell
        v2AdapterService.createSoknad(
            behandlingsId,
            soknadUnderArbeid.opprettetDato,
            eierId,
            kortSoknad,
        )

        // pga. nyModell - opprette soknad før systemdata-updater
        systemdataUpdater.update(soknadUnderArbeid)
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, eierId)

        return StartSoknadResponse(behandlingsId, kortSoknad)
    }

    private fun qualifiesForKortSoknad(
        fnr: String,
        token: String?,
    ): Boolean = hasRecentSoknadFromMetadata(fnr) || hasRecentSoknadFromFiks(token) || hasRecentOrUpcomingUtbetalinger(token)

    private fun isKortSoknadEnabled(): Boolean = unleash.isEnabled("sosialhjelp.soknad.kort_soknad", false)

    private fun hasRecentSoknadFromMetadata(fnr: String): Boolean =
        soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(fnr, LocalDateTime.now(clock).minusDays(120)).any()

    private fun hasRecentSoknadFromFiks(token: String?): Boolean = digisosApiService.qualifiesForKortSoknadThroughSoknader(token, LocalDateTime.now().minusDays(120))

    private fun hasRecentOrUpcomingUtbetalinger(token: String?): Boolean = digisosApiService.qualifiesForKortSoknadThroughUtbetalinger(token, LocalDateTime.now().minusDays(120), LocalDateTime.now().plusDays(14))

    private fun opprettSoknadMetadata(
        fnr: String,
        kortSoknad: Boolean,
    ): String {
        val soknadMetadata =
            SoknadMetadata(
                id = 0,
                behandlingsId = UUID.randomUUID().toString(),
                fnr = fnr,
                skjema = SKJEMANUMMER,
                type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
                status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(clock),
                sistEndretDato = LocalDateTime.now(clock),
                kortSoknad = kortSoknad,
            )
        soknadMetadataRepository.opprett(soknadMetadata)
        return soknadMetadata.behandlingsId
    }

    fun oppdaterSistEndretDatoPaaMetadata(behandlingsId: String?) {
        val hentet = soknadMetadataRepository.hent(behandlingsId)
        hentet?.sistEndretDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(hentet)
    }

    @Transactional
    fun avbrytSoknad(
        behandlingsId: String,
        referer: String?,
    ) {
        log.info("Soknad avbrutt av bruker - slettes")

        val eier = SubjectHandlerUtils.getUserIdFromToken()
        soknadUnderArbeidRepository
            .hentSoknadNullable(behandlingsId, eier)
            ?.let { soknadUnderArbeid ->
                if (mellomlagringService.kanSoknadHaMellomlagredeVedleggForSletting(soknadUnderArbeid)) {
                    mellomlagringService.deleteAllVedlegg(behandlingsId)
                }
                soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, eier)
                settSoknadMetadataAvbrutt(soknadUnderArbeid.behandlingsId, false)
            }
        prometheusMetricsService.reportAvbruttSoknad(referer)

        // ny modell
        v2AdapterService.slettSoknad(behandlingsId)
    }

    fun settSoknadMetadataAvbrutt(
        behandlingsId: String?,
        avbruttAutomatisk: Boolean,
    ) {
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

    fun hentSoknadMetadata(behandlingsId: String): SoknadMetadata = soknadMetadataRepository.hent(behandlingsId) ?: throw IkkeFunnetException("Fant ikke metadata på behandlingsId $behandlingsId")

    companion object {
        private val log = LoggerFactory.getLogger(SoknadServiceOld::class.java)

        fun createEmptyJsonInternalSoknad(
            eier: String,
            kortSoknad: Boolean,
        ): JsonInternalSoknad =
            JsonInternalSoknad()
                .withSoknad(
                    JsonSoknad()
                        .withData(
                            JsonData()
                                .withSoknadstype(if (kortSoknad) JsonData.Soknadstype.KORT else JsonData.Soknadstype.STANDARD)
                                .withPersonalia(
                                    JsonPersonalia()
                                        .withPersonIdentifikator(
                                            JsonPersonIdentifikator()
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                                .withVerdi(eier),
                                        ).withNavn(
                                            JsonSokernavn()
                                                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                                .withFornavn("")
                                                .withMellomnavn("")
                                                .withEtternavn(""),
                                        ).withKontonummer(
                                            JsonKontonummer()
                                                .withKilde(JsonKilde.SYSTEM),
                                        ),
                                ).withArbeid(JsonArbeid())
                                .withUtdanning(
                                    JsonUtdanning()
                                        .withKilde(JsonKilde.BRUKER),
                                ).withFamilie(
                                    JsonFamilie()
                                        .withForsorgerplikt(JsonForsorgerplikt()),
                                ).withBegrunnelse(
                                    JsonBegrunnelse()
                                        .withKilde(JsonKildeBruker.BRUKER)
                                        .withHvorforSoke("")
                                        .withHvaSokesOm(""),
                                ).withBosituasjon(
                                    JsonBosituasjon()
                                        .withKilde(JsonKildeBruker.BRUKER),
                                ).withOkonomi(
                                    JsonOkonomi()
                                        .withOpplysninger(
                                            JsonOkonomiopplysninger()
                                                .withUtbetaling(ArrayList())
                                                .withUtgift(ArrayList())
                                                .withBostotte(JsonBostotte())
                                                .withBekreftelse(ArrayList()),
                                        ).withOversikt(
                                            JsonOkonomioversikt()
                                                .withInntekt(ArrayList())
                                                .withUtgift(ArrayList())
                                                .withFormue(ArrayList()),
                                        ),
                                ),
                        ).withMottaker(
                            JsonSoknadsmottaker()
                                .withNavEnhetsnavn("")
                                .withEnhetsnummer(""),
                        ).withDriftsinformasjon(
                            JsonDriftsinformasjon()
                                .withUtbetalingerFraNavFeilet(false)
                                .withInntektFraSkatteetatenFeilet(false)
                                .withStotteFraHusbankenFeilet(false),
                        ).withKompatibilitet(ArrayList()),
                ).withVedlegg(JsonVedleggSpesifikasjon())
    }
}
