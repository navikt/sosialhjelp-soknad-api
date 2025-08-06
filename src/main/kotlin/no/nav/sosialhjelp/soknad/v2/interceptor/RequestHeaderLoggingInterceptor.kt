import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.config.SoknadApiHandlerInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RequestHeaderLoggingInterceptor : SoknadApiHandlerInterceptor {
    @Throws(Exception::class)
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        logger.info("--- Incoming Request Headers ---")
        val headerNames = request.getHeaderNames()
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            logger.info("{}: {}", headerName, request.getHeader(headerName))
        }
        logger.info("--------------------------------")
        return true // Continue processing the request
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RequestHeaderLoggingInterceptor::class.java)
    }
}
