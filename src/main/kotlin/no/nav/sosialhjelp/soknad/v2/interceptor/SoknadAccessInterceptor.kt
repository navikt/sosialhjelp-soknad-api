package no.nav.sosialhjelp.soknad.v2.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.config.SoknadApiHandlerInterceptor
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerMapping
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class SoknadAccessInterceptor(
    private val soknadService: SoknadService,
    private val metadataService: SoknadMetadataService,
) : SoknadApiHandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == "OPTIONS") return true

        getSoknadId(request)
            ?.convertStringToUUID()
            ?.also { id -> checkValidSoknadStatus(soknadId = id) }
            ?.also { id -> checkPersonId(soknadId = id, method = request.method) }

        return true
    }

    private fun checkValidSoknadStatus(soknadId: UUID) {
        metadataService.getMetadataForSoknad(soknadId).status
            .also { status ->
                if (OPEN_STATUS.none { it == status }) {
                    throw AuthorizationException(message = "Soknaden er allerede sendt inn: $status")
                }
            }
    }

    private fun checkPersonId(
        soknadId: UUID,
        method: String,
    ) {
        soknadService.findOrError(soknadId).eierPersonId
            .also { eier ->
                if (method in listOf("GET", "POST", "PUT", "DELETE") && eier != personId()) {
                    throw AuthorizationException("Bruker har ikke tilgang til søknaden")
                }
            }
    }

    companion object {
        private val OPEN_STATUS = listOf(SoknadStatus.OPPRETTET, SoknadStatus.INNSENDING_FEILET)
    }
}

private fun getSoknadId(request: HttpServletRequest): String? {
    val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
    return pathVariables?.get("soknadId") as String?
}

private fun String.convertStringToUUID(): UUID =
    runCatching { UUID.fromString(this) }
        .getOrElse { e ->
            if (e is IllegalArgumentException && e.message?.contains("Invalid UUID string") == true) {
                throw AuthorizationException("Ugyldig format på SoknadId: $this")
            }
            throw e
        }
