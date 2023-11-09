package no.nav.sosialhjelp.soknad.nymodell.app.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.repository.SoknadRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import java.util.*

class SoknadEierInterceptor(
    val soknadRepository: SoknadRepository
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // hent ut soknadId fra path
        val pathVariableMap = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<*, *>
        val soknadId = (pathVariableMap["soknadId"] as String).let { UUID.fromString(it) }

        // TODO Sjekk om person har adressebeskyttelse

//         hvis personen eier søknaden - return true
        soknadRepository.findByIdOrNull(soknadId)?.let {
            val pid = SubjectHandlerUtils.getUserIdFromToken()
            if (it.eier.personId == pid) return true
        } ?: throw IkkeFunnetException(melding = "Soknaden finnes ikke.")

//         i alle andre tilfeller - return false
        setErrorResponse(response)
        return false
    }

    private fun setErrorResponse(response: HttpServletResponse) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val feilEierError = FeilEierError("Personen eier ikke denne soknaden.")
        response.writer.write(ObjectMapper().writeValueAsString(feilEierError))
    }
}

internal data class FeilEierError(
    val errorMessage: String
)