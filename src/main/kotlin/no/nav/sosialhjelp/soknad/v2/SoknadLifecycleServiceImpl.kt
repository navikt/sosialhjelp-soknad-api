package no.nav.sosialhjelp.soknad.v2

import io.getunleash.Unleash
import no.nav.sosialhjelp.soknad.app.exceptions.FeilVedSendingTilFiksException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
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
    private val unleash: Unleash,
    private val digisosApiService: DigisosApiService,
) : SoknadLifecycleService {
    override fun startSoknad(): Pair<UUID, Boolean> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()

        val kortSoknad = isKortSoknadEnabled() && qualifiesForKortSoknad(fnr)

        prometheusMetricsService.reportStartSoknad(kortSoknad)

        val soknadId =
            fnr.let {
                soknadServiceImpl.createSoknad(
                    eierId = it,
                    soknadId = UUID.randomUUID(),
                    // TODO Spesifisert til UTC i filformatet
                    opprettetDato = LocalDateTime.now(),
                    kortSoknad = kortSoknad,
                )
            }

        MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, soknadId.toString())

        return soknadId to kortSoknad
    }

    private fun isKortSoknadEnabled(): Boolean = unleash.isEnabled("sosialhjelp.soknad.kort_soknad", false)

    private fun qualifiesForKortSoknad(fnr: String): Boolean = hasRecentSoknadFromMetadata(fnr) || hasRecentSoknadFromFiks(fnr) || hasRecentOrUpcomingUtbetalinger(fnr)

    private fun hasRecentSoknadFromMetadata(fnr: String): Boolean =
        soknadServiceImpl.hasSoknadNewerThan(
            eierId = fnr,
            tidspunkt = LocalDateTime.now().minusDays(120),
        )

    private fun hasRecentSoknadFromFiks(fnr: String): Boolean = digisosApiService.qualifiesForKortSoknadThroughSoknader(fnr, LocalDateTime.now().minusDays(120))

    private fun hasRecentOrUpcomingUtbetalinger(fnr: String): Boolean = digisosApiService.qualifiesForKortSoknadThroughUtbetalinger(fnr, LocalDateTime.now().minusDays(120), LocalDateTime.now().plusDays(14))

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
