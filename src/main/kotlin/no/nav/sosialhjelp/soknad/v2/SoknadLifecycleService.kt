package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.lifecycle.CreateDeleteSoknadHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

interface SoknadLifecycleService {
    fun startSoknad(
        token: String,
        isKort: Boolean,
    ): UUID

    fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    )

    fun sendSoknad(soknadId: UUID): Pair<UUID, LocalDateTime>
}

@Service
@Transactional
class SoknadLifecycleServiceImpl(
    private val prometheusMetricsService: PrometheusMetricsService,
    private val createDeleteSoknadHandler: CreateDeleteSoknadHandler,
    private val sendSoknadHandler: SendSoknadHandler,
) : SoknadLifecycleService {
    override fun startSoknad(
        token: String,
        isKort: Boolean,
    ): UUID {
        // TODO Metadata

        return createDeleteSoknadHandler
            .createSoknad(token, isKort)
            .also { soknadId ->
                prometheusMetricsService.reportStartSoknad()
                MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, soknadId.toString())
                logger.info("Ny søknad opprettet")
            }
    }

    override fun sendSoknad(soknadId: UUID): Pair<UUID, LocalDateTime> {
        // TODO Metadata
        logger.info("Starter innsending av søknad.")

        val sendtInfo =
            runCatching { sendSoknadHandler.doSendAndReturnInfo(soknadId) }
                .onFailure {
                    prometheusMetricsService.reportFeilet()
                    logger.error("Feil ved sending av søknad.", it)
                }.getOrThrow()

        prometheusMetricsService.reportSendt(sendtInfo.isKortSoknad)
        prometheusMetricsService.reportSoknadMottaker(
            MetricsUtils.navKontorTilMetricNavn(sendtInfo.navEnhet.enhetsnavn),
        )

        // TODO Pr. dags dato skal en søknad slettes ved innsending - i fremtiden skal den slettes ved mottatt kvittering
        createDeleteSoknadHandler.deleteSoknad(soknadId)

        return Pair(sendtInfo.digisosId, sendtInfo.innsendingTidspunkt)
    }

    override fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    ) {
        // TODO Metadata

        logger.info("Søknad avbrutt. Sletter data.")

        createDeleteSoknadHandler.cancelSoknad(soknadId)
        prometheusMetricsService.reportAvbruttSoknad(referer)
    }

    companion object {
        private val logger by logger()
    }
}
