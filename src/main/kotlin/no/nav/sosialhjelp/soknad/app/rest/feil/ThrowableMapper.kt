// package no.nav.sosialhjelp.soknad.app.rest.feil
//
// import no.nav.sosialhjelp.soknad.app.exceptions.SamtidigOppdateringException
// import org.slf4j.LoggerFactory
// import org.springframework.beans.factory.annotation.Value
// import org.springframework.stereotype.Component
// import java.net.URI
// import javax.ws.rs.NotFoundException
// import javax.ws.rs.WebApplicationException
// import javax.ws.rs.core.MediaType
// import javax.ws.rs.core.Response
// import javax.ws.rs.ext.ExceptionMapper
// import javax.ws.rs.ext.Provider
//
// @Provider
// @Component
// class ThrowableMapper(
//    @Value("\${loginservice.url}") private val loginserviceUrl: String
// ) : ExceptionMapper<Throwable> {
//
//    override fun toResponse(e: Throwable): Response {
//        return when (e) {
//            is WebApplicationException -> {
//                val status = e.response.status
//                if (status == Response.Status.UNAUTHORIZED.statusCode) {
//                    logger.debug(e.message, e)
//                    return createUnauthorizedWithLoginLocationResponse("Autentiseringsfeil")
//                } else if (status == Response.Status.FORBIDDEN.statusCode) {
//                    logger.debug(e.message, e)
//                    return createUnauthorizedWithLoginLocationResponse("Autoriseringsfeil")
//                } else if (e is NotFoundException) {
//                    logger.warn(e.message, e)
//                } else {
//                    logger.error(e.message, e)
//                }
//                Response.status(e.response.status)
//                    .type(MediaType.APPLICATION_JSON)
//                    .entity(Feilmelding(WEB_APPLICATION_ERROR, "Noe uventet feilet"))
//                    .build()
//            }
//            is SamtidigOppdateringException -> {
//                logger.warn(e.message, e)
//                Response.status(Response.Status.CONFLICT)
//                    .type(MediaType.APPLICATION_JSON)
//                    .entity(Feilmelding(WEB_APPLICATION_ERROR, "Samtidig oppdatering av søknad"))
//                    .build()
//            }
//            else -> {
//                logger.error("Noe uventet feilet: ${e.message}", e)
//                Response.serverError()
//                    .header(Feilmelding.NO_BIGIP_5XX_REDIRECT, true)
//                    .type(MediaType.APPLICATION_JSON)
//                    .entity(Feilmelding("unexpected_error", "Noe uventet feilet"))
//                    .build()
//            }
//        }
//    }
//
//    private fun createUnauthorizedWithLoginLocationResponse(message: String): Response {
//        val loginUrl = URI.create(loginserviceUrl)
//        return Response.status(Response.Status.UNAUTHORIZED.statusCode)
//            .location(loginUrl)
//            .type(MediaType.APPLICATION_JSON)
//            .entity(UnauthorizedMelding(WEB_APPLICATION_ERROR, message, loginUrl))
//            .build()
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(ThrowableMapper::class.java)
//        private const val WEB_APPLICATION_ERROR = "web_application_error"
//    }
// }
