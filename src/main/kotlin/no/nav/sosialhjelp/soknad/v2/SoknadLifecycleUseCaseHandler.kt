package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.InnsendingFeiletException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadLifecycleException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DocumentValidator
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentlagerService
import no.nav.sosialhjelp.soknad.v2.lifecycle.CreateDeleteSoknadHandler
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
    private val createDeleteSoknadHandler: CreateDeleteSoknadHandler,
    private val sendSoknadHandler: SendSoknadHandler,
    private val documentValidator: DocumentValidator,
    private val dokumentlagerService: DokumentlagerService,
) : SoknadLifecycleUseCaseHandler {
    override fun startSoknad(isKort: Boolean): UUID {
        // legger det i MDC manuelt siden det ikke finnes i request enda
        val soknadId = UUID.randomUUID().also { MdcOperations.putToMDC(MdcOperations.MDC_SOKNAD_ID, it.toString()) }

        return runCatching { createDeleteSoknadHandler.createSoknad(soknadId, isKort) }
            .onSuccess {
                createDeleteSoknadHandler.runRegisterDataFetchers(soknadId)

                prometheusMetricsService.reportStartSoknad()
                logger.info("Ny søknad opprettet")
            }
            .onFailure {
                prometheusMetricsService.reportFeilet()
                throw SoknadLifecycleException("Feil ved opprettelse av søknad.", it, soknadId)
            }
            .also { MdcOperations.clearMDC() }
            .getOrThrow()
    }

    override fun sendSoknad(
        soknadId: UUID,
    ): Pair<UUID, LocalDateTime> {
        logger.info("Starter innsending av søknad.")

        documentValidator.validateDocumentsExistsInMellomlager(soknadId)

        return runCatching { sendSoknadHandler.doSendAndReturnInfo(soknadId) }
            .onSuccess {
                prometheusMetricsService.reportSendt(it.isKortSoknad)

                prometheusMetricsService.reportSoknadMottaker(
                    MetricsUtils.navKontorTilMetricNavn(it.navEnhet.enhetsnavn),
                )
            }
            .getOrElse { e ->
                when (e) {
                    is SoknadAlleredeSendtException -> e.sendtInfo
                    else -> {
                        prometheusMetricsService.reportFeilet()
                        throw InnsendingFeiletException(
                            deletionDate = sendSoknadHandler.getDeletionDate(soknadId),
                            message = "Feil ved innsending av søknad.",
                            throwable = e,
                            id = soknadId,
                        )
                    }
                }
            }
            .let { Pair(it.digisosId, it.innsendingTidspunkt) }
    }

    override fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    ) {
        runCatching { createDeleteSoknadHandler.cancelSoknad(soknadId) }
            .onSuccess {
                dokumentlagerService.deleteAllDokumenterForSoknad(soknadId)
                prometheusMetricsService.reportAvbruttSoknad(referer)
                logger.info("Søknad avbrutt. Sletter data.")
            }
            .onFailure {
                throw SoknadLifecycleException("Feil ved avbrutt søknad.", it, soknadId)
            }
    }

    companion object {
        private val logger by logger()
    }
}
