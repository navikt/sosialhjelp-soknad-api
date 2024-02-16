package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.FeilVedSendingTilFiksException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class SoknadLifecycleServiceImpl(
    private val prometheusMetricsService: PrometheusMetricsService,
    private val registerDataFetcher: RegisterDataFetcher,
    private val soknadService: SoknadService,
) : SoknadLifecycleService {
    override fun startSoknad(): UUID {
        prometheusMetricsService.reportStartSoknad()

        val soknadId = SubjectHandlerUtils.getUserIdFromToken().let {
            soknadService.createSoknad(eierId = it)
        }

        MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, soknadId.toString())
        registerDataFetcher.updateRegisterData(soknadId)

        return soknadId
    }

    override fun cancelSoknad(soknadId: UUID, referer: String?) {
        soknadService.deleteSoknad(soknadId)
        prometheusMetricsService.reportAvbruttSoknad(referer)
    }

    override fun sendSoknad(soknadId: UUID): Pair<UUID, LocalDateTime> {
        val digisosId = try {
            soknadService.sendSoknad(soknadId)
        } catch (e: FeilVedSendingTilFiksException) {
            prometheusMetricsService.reportFeilet()
            throw e
        }

        prometheusMetricsService.reportSendt()
        prometheusMetricsService.reportSoknadMottaker(
            MetricsUtils.navKontorTilMetricNavn(
                soknadService.getSoknad(soknadId).mottaker.enhetsnavn
            )
        )

        return Pair(digisosId, LocalDateTime.now())
    }

    companion object {
        private val log by logger()
    }
}
