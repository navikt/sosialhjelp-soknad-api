package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.exceptions.FeilVedSendingTilFiksException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.soknad.service.SoknadService

@Service
class SoknadLifecycleServiceImpl(
    private val prometheusMetricsService: PrometheusMetricsService,
    private val registerDataService: RegisterDataService,
    private val soknadServiceImpl: SoknadService,
    private val adresseService: AdresseService,
) : SoknadLifecycleService {
    override fun startSoknad(): UUID {
        prometheusMetricsService.reportStartSoknad()

        val soknadId =
            SubjectHandlerUtils.getUserIdFromToken().let {
                soknadServiceImpl.createSoknad(eierId = it)
            }

        MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, soknadId.toString())

        return soknadId
    }

    override fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    ) {
        soknadServiceImpl.deleteSoknad(soknadId)
        prometheusMetricsService.reportAvbruttSoknad(referer)
    }

    override fun sendSoknad(soknadId: UUID): Pair<UUID, LocalDateTime> {
        val digisosId =
            try {
                soknadServiceImpl.sendSoknad(soknadId)
            } catch (e: FeilVedSendingTilFiksException) {
                prometheusMetricsService.reportFeilet()
                throw e
            }

        prometheusMetricsService.reportSendt()
        prometheusMetricsService.reportSoknadMottaker(
            MetricsUtils.navKontorTilMetricNavn(
                adresseService.findMottaker(soknadId)?.enhetsnavn,
            ),
        )

        return Pair(digisosId, LocalDateTime.now())
    }
}
