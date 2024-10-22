package no.nav.sosialhjelp.soknad.app

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.HandlerMapping

fun HttpServletRequest.getBehandlingsId(): String? {
    val pathVariables = getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
    return pathVariables?.get("behandlingsId") as String?
}
