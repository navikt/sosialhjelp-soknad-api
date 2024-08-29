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
    fun startSoknad(): UUID

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
    override fun startSoknad(): UUID {
        prometheusMetricsService.reportStartSoknad()
        // TODO Metadata

        return createDeleteSoknadHandler.createSoknad()
            .also {
                MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, it.toString())
                logger.info("Ny søknad opprettet")
            }
    }

    override fun sendSoknad(soknadId: UUID): Pair<UUID, LocalDateTime> {
        // TODO Metadata
        logger.info("Starter innsending av søknad.")

        val (digisosId, navEnhet) =
            runCatching { sendSoknadHandler.doSendAndReturnDigisosId(soknadId) }
                .onFailure {
                    prometheusMetricsService.reportFeilet()
                    logger.error("Feil ved sending av søknad.", it)
                    throw it
                }
                .getOrThrow()

        prometheusMetricsService.reportSendt()
        prometheusMetricsService.reportSoknadMottaker(
            MetricsUtils.navKontorTilMetricNavn(navEnhet.enhetsnavn),
        )

        // TODO Pr. dags dato skal en søknad slettes ved innsending - i fremtiden skal den slettes ved mottatt kvittering
        createDeleteSoknadHandler.deleteSoknad(soknadId)

        return Pair(digisosId, LocalDateTime.now())
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
