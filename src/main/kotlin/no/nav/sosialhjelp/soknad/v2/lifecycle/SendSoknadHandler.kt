package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.AlleredeMottattException
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil
import no.nav.sosialhjelp.soknad.v2.SoknadValidator
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class SendSoknadHandler(
    private val jsonGenerator: JsonInternalSoknadGenerator,
    private val soknadValidator: SoknadValidator,
    private val sendSoknadManager: SendSoknadManager,
    private val metadataService: SoknadMetadataService,
) {
    fun doSendAndReturnInfo(
        soknadId: UUID,
    ): SoknadSendtInfo {
        soknadValidator.validateSoknad(soknadId)

        val innsendingstidspunkt = metadataService.setInnsendingstidspunkt(soknadId, nowWithMillis())

        val json = jsonGenerator.createJsonInternalSoknad(soknadId)
        val navEnhet = sendSoknadManager.getNavEnhetForSending(soknadId)

        val digisosId =
            runCatching { sendSoknadManager.doSendSoknad(soknadId, json, navEnhet.kommunenummer) }
                .onSuccess { digisosId ->
                    metadataService.updateSoknadSendt(
                        soknadId = soknadId,
                        kommunenummer = navEnhet.kommunenummer,
                        digisosId = digisosId,
                        innsendingsTidspunkt = innsendingstidspunkt,
                    )
                }
                .onFailure { e -> handleError(soknadId, navEnhet.enhetsnavn, e) }
                .getOrThrow()

        json.checkDuplicateUtbetalinger()

        logger.info("Sendt (kort=${metadataService.isKortSoknad(soknadId)}) søknad til FIKS med DigisosId: $digisosId")

        VedleggskravStatistikkUtil.genererVedleggskravStatistikk(json)

        return metadataService.getSoknadSendtInfo(soknadId).copy(navEnhetNavn = navEnhet.enhetsnavn)
    }

    fun getDeletionDate(soknadId: UUID): LocalDateTime {
        return metadataService.getMetadataForSoknad(soknadId)
            .run {
                when (status) {
                    SoknadStatus.INNSENDING_FEILET -> tidspunkt.opprettet.plusDays(19)
                    else -> tidspunkt.opprettet.plusDays(14)
                }
            }
    }

    private fun handleError(
        soknadId: UUID,
        navEnhetNavn: String?,
        e: Throwable,
    ): Nothing {
        when (e) {
            is AlleredeMottattException -> createSoknadAlleredeSendtException(soknadId, navEnhetNavn)
            else -> {
                metadataService.updateSendingFeilet(soknadId)
                throw e
            }
        }
    }

    private fun createSoknadAlleredeSendtException(
        soknadId: UUID,
        navEnhetNavn: String?,
    ): Nothing {
        metadataService.getSoknadSendtInfo(soknadId)
            .also { info ->
                throw SoknadAlleredeSendtException(
                    sendtInfo = info.copy(navEnhetNavn = navEnhetNavn),
                    message = "Søknad med ID $soknadId er allerede sendt.",
                )
            }
    }

    companion object {
        internal val logger by logger()
    }
}

private fun SoknadMetadataService.isKortSoknad(soknadId: UUID) = getSoknadType(soknadId) == SoknadType.KORT

data class SoknadSendtInfo(
    val digisosId: UUID,
    val navEnhetNavn: String?,
    val isKortSoknad: Boolean,
    val innsendingTidspunkt: LocalDateTime,
)

private fun JsonInternalSoknad.checkDuplicateUtbetalinger() {
    val duplicates =
        soknad.data.okonomi.opplysninger.utbetaling
            .groupBy { listOf(it.tittel, it.utbetalingsdato, it.netto, it.brutto) }
            .filter { it.value.size > 1 }

    val totalDuplicatesCount = duplicates.values.sumOf { it.size }

    if (totalDuplicatesCount > 0) {
        SendSoknadHandler.logger.info(
            "Søknad sendt, ut av ${soknad.data.okonomi.opplysninger.utbetaling.size} " +
                "utbetaling(er) så er det $totalDuplicatesCount som er identiske utbetaling(er)",
        )
    }
}

data class NavEnhetForSending(
    val kommunenummer: String,
    val enhetsnavn: String?,
)
