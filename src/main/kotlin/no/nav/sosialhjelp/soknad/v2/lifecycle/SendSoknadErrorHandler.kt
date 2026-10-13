package no.nav.sosialhjelp.soknad.v2.lifecycle

import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.exceptions.BrokenSoknadException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.SendSoknadResponse
import no.nav.sosialhjelp.soknad.v2.lifecycle.SendSoknadHandler.Companion.logger
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
import java.util.UUID

object SendSoknadErrorHandler {
    fun handleInnsendingError(
        soknadId: UUID,
        response: SendSoknadResponse,
    ): Nothing {
        when (response) {
            is SendSoknadResponse.FiksError -> handleFiksError(soknadId, response)
            is SendSoknadResponse.Error -> handleError(soknadId, response)
            is SendSoknadResponse.Success -> error("Success is not a bad thing")
        }
    }

    private fun handleError(
        soknadId: UUID,
        response: SendSoknadResponse.Error,
    ): Nothing {
        throw FiksException("Opplasting av $soknadId til fiks-digisos-api feilet etter ${System.currentTimeMillis() - response.startTime} ms", response.e)
    }

    private fun handleFiksError(
        soknadId: UUID,
        response: SendSoknadResponse.FiksError,
    ): Nothing {
        val feilmelding =
            "Opplasting av $soknadId til fiks-digisos-api feilet etter ${System.currentTimeMillis() - response.startTime} " +
                "ms med status ${response.errorMessage.status} og response: ${response.errorMessage}"

        if (response.e is BadRequest) resolveBadRequest(soknadId, response, feilmelding)

        throw FiksException(message = feilmelding, cause = response.e)
    }

    // BadRequest
    private fun resolveBadRequest(
        soknadId: UUID,
        response: SendSoknadResponse.FiksError,
        feilmelding: String,
    ) {
        response.errorMessage.message?.let {
            val digisosId = FiksUtils.isAlleredeMottatt(it, soknadId)
            if (digisosId != null) handleAlleredeMottatt(digisosId = digisosId, soknadId = soknadId, errorResponse = it)
            if (FiksUtils.isMottakPabegynt(it, soknadId)) handleMottakPabegynt(soknadId = soknadId, errorResponse = it)
        }
        throw BrokenSoknadException("Broken søknad: $feilmelding")
    }

    private fun handleAlleredeMottatt(
        digisosId: UUID,
        soknadId: UUID,
        errorResponse: String?,
    ): Nothing {
        logger.warn(
            "Søknad $soknadId er allerede sendt med id $digisosId. " +
                "Returner exception med digisos-id så brukeren blir rutet til innsyn. " +
                "ErrorResponse var: $errorResponse",
        )
        throw AlleredeMottattException(
            digisosId = digisosId,
            message = "Søknad $soknadId er allerede sendt med id $digisosId. ErrorResponse var: $errorResponse",
        )
    }

    private fun handleMottakPabegynt(
        soknadId: UUID,
        errorResponse: String,
    ): Nothing {
        logger.warn("Mottak av søknad $soknadId er allerede påbegynt hos FIKS.")
        throw MottakPabegyntException("Søknad $soknadId er allerede påbegynt hos FIKS. ErrorResponse var: $errorResponse")
    }
}
