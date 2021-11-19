package no.nav.sosialhjelp.soknad.client.pdl

import com.fasterxml.jackson.databind.JsonNode
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokDataDto
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.HentGeografiskTilknytning

sealed class BaseResponse(
    open val errors: List<JsonNode>?
) {
    fun checkForPdlApiErrors() {
        errors?.let { handleErrors(it) }
    }

    private fun handleErrors(errors: List<JsonNode>) {
        val errorMessage = errors
            .map { "${it.get("message")} (feilkode: ${it.path("extensions").path("code")})" }
            .joinToString(prefix = "Error i respons fra pdl-api: ", separator = ", ") { it }
        throw PdlApiException(errorMessage)
    }
}

data class HentGeografiskTilknytningDto(
    val data: HentGeografiskTilknytning?,
    override val errors: List<JsonNode>?
) : BaseResponse(errors)

data class AdressesokDto(
    val data: AdressesokDataDto?,
    override val errors: List<JsonNode>?
) : BaseResponse(errors)
