package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.exceptions.FeilVedSendingTilFiksException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.soknad.service.SoknadService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class SoknadLifecycleServiceImpl(
    private val prometheusMetricsService: PrometheusMetricsService,
    private val soknadServiceImpl: SoknadService,
    private val adresseService: AdresseService,
) : SoknadLifecycleService {
    override fun startSoknad(): Pair<UUID, Boolean> {
        prometheusMetricsService.reportStartSoknad()

        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val hasSoknadNewerThan4Months =
            soknadServiceImpl.hasSoknadNewerThan(
                eierId = fnr,
                tidspunkt = LocalDateTime.now().minusMonths(4),
            )
        val soknadId =
            fnr.let {
                soknadServiceImpl.createSoknad(
                    eierId = it,
                    soknadId = UUID.randomUUID(),
                    // TODO Spesifisert til UTC i filformatet
                    opprettetDato = LocalDateTime.now(),
                    kortSoknad = hasSoknadNewerThan4Months,
                )
            }

        MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, soknadId.toString())

        return soknadId to hasSoknadNewerThan4Months
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
