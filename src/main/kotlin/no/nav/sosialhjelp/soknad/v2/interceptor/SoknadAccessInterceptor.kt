package no.nav.sosialhjelp.soknad.v2.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.config.SoknadApiHandlerInterceptor
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.v2.SoknadSendtInfo
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
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

        getSoknadId(request)?.also { checkPersonId(it.convertIdToUUID(), request.method) }

        return true
    }

    private fun checkPersonId(
        soknadId: UUID,
        method: String,
    ) {
        findSoknadOrCheckHistory(soknadId).eierPersonId
            .also { eier ->
                if (method in listOf("GET", "POST", "PUT", "DELETE") && eier != personId()) {
                    throw AuthorizationException("Bruker har ikke tilgang til søknaden")
                }
            }
    }

    private fun findSoknadOrCheckHistory(soknadId: UUID): Soknad =
        runCatching { soknadService.findOrError(soknadId) }
            .getOrElse { e -> if (e is IkkeFunnetException) handleIkkeFunnetException(soknadId, e) else throw e }

    private fun handleIkkeFunnetException(
        soknadId: UUID,
        e: IkkeFunnetException,
    ): Nothing {
        metadataService.getMetadataForSoknad(soknadId)
            .also { metadata ->
                when (metadata.status) {
                    SoknadStatus.SENDT, SoknadStatus.MOTTATT_FSL ->
                        throw SoknadAlleredeSendtException(
                            message = "Soknad er allerede sendt",
                            sendtInfo =
                                SoknadSendtInfo(
                                    digisosId = metadata.digisosId ?: error("Mangler digisosId"),
                                    navEnhet = NavEnhet(kommunenummer = metadata.mottakerKommunenummer),
                                    isKortSoknad = metadata.soknadType == SoknadType.KORT,
                                    innsendingTidspunkt = metadata.tidspunkt.sendtInn ?: error("Mangler innsendingstidspunkt"),
                                ),
                        )
                    else -> throw e
                }
            }
    }
}

private fun getSoknadId(request: HttpServletRequest): String? {
    val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
    return pathVariables?.get("soknadId") as String?
}

private fun String.convertIdToUUID(): UUID =
    runCatching { UUID.fromString(this) }
        .getOrElse { e ->
            if (e is IllegalArgumentException && e.message?.contains("Invalid UUID string") == true) {
                throw AuthorizationException("Ugyldig format på SoknadId: $this")
            }
            throw e
        }
