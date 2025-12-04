package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.InnsendingFeiletException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadLifecycleException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.lifecycle.CancelSoknadHandler
import no.nav.sosialhjelp.soknad.v2.lifecycle.CreateSoknadHandler
import no.nav.sosialhjelp.soknad.v2.lifecycle.SendSoknadHandler
import no.nav.sosialhjelp.soknad.v2.lifecycle.SoknadSendtInfo
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

interface SoknadLifecycleUseCaseHandler {
    fun startSoknad(
        isKort: Boolean,
    ): UUID

    fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    )

    fun sendSoknad(
        soknadId: UUID,
    ): Pair<UUID, LocalDateTime>
}

@Service
class SoknadLifecycleHandlerImpl(
    private val prometheusMetricsService: PrometheusMetricsService,
    private val createSoknadHandler: CreateSoknadHandler,
    private val sendSoknadHandler: SendSoknadHandler,
    private val cancelSoknadHandler: CancelSoknadHandler,
) : SoknadLifecycleUseCaseHandler {
    override fun startSoknad(isKort: Boolean): UUID {
        // legger det i MDC manuelt siden det ikke finnes i request enda
        val soknadId = UUID.randomUUID().also { MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, it.toString()) }

        return runCatching { createSoknadHandler.createSoknad(soknadId, isKort) }
            .onSuccess {
                createSoknadHandler.runRegisterDataFetchers(soknadId)

                prometheusMetricsService.reportStartSoknad()
                logger.info("Ny søknad opprettet")
            }
            .onFailure {
                if (it is AuthorizationException) {
                    throw it
                } else {
                    prometheusMetricsService.reportStartSoknadFeilet()
                    throw SoknadLifecycleException("Feil ved opprettelse av søknad.", it, soknadId)
                }
            }
            .also { MdcOperations.clearMDC() }
            .getOrThrow()
    }

    override fun sendSoknad(
        soknadId: UUID,
    ): Pair<UUID, LocalDateTime> {
        logger.info("Starter innsending av søknad.")

        return runCatching { sendSoknadHandler.doSendAndReturnInfo(soknadId) }
            .onSuccess {
                prometheusMetricsService.reportSendt(it.isKortSoknad)

                prometheusMetricsService.reportSoknadMottaker(
                    MetricsUtils.navKontorTilMetricNavn(it.navEnhetNavn),
                )
            }
            .getOrElse { e -> handleError(soknadId, e) }
            .let { Pair(it.digisosId, it.innsendingTidspunkt) }
    }

    override fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    ) {
        runCatching { cancelSoknadHandler.cancelSoknad(soknadId) }
            .onSuccess {
                cancelSoknadHandler.cleanUploadedDocuments(soknadId)
                prometheusMetricsService.reportAvbruttSoknad(referer)
                logger.info("Søknad avbrutt. Sletter data.")
            }
            .onFailure {
                throw SoknadLifecycleException("Feil ved avbrutt søknad.", it, soknadId)
            }
    }

    private fun handleError(
        soknadId: UUID,
        e: Throwable,
    ): SoknadSendtInfo {
        return when (e) {
            is SoknadAlleredeSendtException -> e.sendtInfo
            is SendingTilKommuneUtilgjengeligException, is SendingTilKommuneErMidlertidigUtilgjengeligException,
            -> throw e
            else -> {
                prometheusMetricsService.reportSendSoknadFeilet()
                throw InnsendingFeiletException(
                    deletionDate = sendSoknadHandler.getDeletionDate(soknadId),
                    message = "Feil ved innsending av søknad.",
                    throwable = e,
                    id = soknadId,
                )
            }
        }
    }

    companion object {
        private val logger by logger()
    }
}
