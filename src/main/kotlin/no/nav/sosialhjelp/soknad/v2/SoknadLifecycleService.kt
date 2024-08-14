package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
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
    private val soknadService: SoknadService,
    private val adresseService: AdresseService,
    private val sendSoknadHandler: SendSoknadHandler,
    private val registerDataService: RegisterDataService,
) : SoknadLifecycleService {
    override fun startSoknad(): UUID {
        prometheusMetricsService.reportStartSoknad()

        val soknadId =
            SubjectHandlerUtils.getUserIdFromToken().let {
                soknadService.createSoknad(
                    eierId = it,
                    soknadId = UUID.randomUUID(),
                    // TODO Spesifisert til UTC i filformatet
                    opprettetDato = LocalDateTime.now(),
                )
            }.also { MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, it.toString()) }

        // TODO Må vel hente eksterne data (spesielt person-data) et eller annet sted på dette tidspunktet?
        registerDataService.runAllRegisterDataFetchers(soknadId)

        return soknadId
    }

    override fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    ) {
        soknadService.deleteSoknad(soknadId)
        prometheusMetricsService.reportAvbruttSoknad(referer)
    }

    override fun sendSoknad(soknadId: UUID): Pair<UUID, LocalDateTime> {
        val digisosId =
            runCatching {
                sendSoknadHandler.doSendAndReturnDigisosId(
                    soknad = soknadService.getSoknad(soknadId),
                )
            }
                .onFailure {
                    prometheusMetricsService.reportFeilet()
                    logger.error("Feil ved sending av søknad", it)
                    throw it
                }
                .getOrThrow()

        prometheusMetricsService.reportSendt()
        prometheusMetricsService.reportSoknadMottaker(
            MetricsUtils.navKontorTilMetricNavn(
                adresseService.findMottaker(soknadId)?.enhetsnavn,
            ),
        )

        return Pair(digisosId, LocalDateTime.now())
    }

    companion object {
        private val logger by logger()
    }
}
