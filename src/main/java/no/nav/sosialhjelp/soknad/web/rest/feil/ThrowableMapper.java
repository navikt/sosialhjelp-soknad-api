//package no.nav.sosialhjelp.soknad.web.rest.feil;
//
//import no.nav.sosialhjelp.soknad.business.exceptions.SamtidigOppdateringException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.ws.rs.NotFoundException;
//import javax.ws.rs.WebApplicationException;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.ext.ExceptionMapper;
//import javax.ws.rs.ext.Provider;
//import java.net.URI;
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static javax.ws.rs.core.Response.Status.FORBIDDEN;
//import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
//import static javax.ws.rs.core.Response.serverError;
//import static javax.ws.rs.core.Response.status;
//import static no.nav.sosialhjelp.soknad.web.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;
//
//@Provider
//public class ThrowableMapper implements ExceptionMapper<Throwable> {
//    private static final Logger logger = LoggerFactory.getLogger(ThrowableMapper.class);
//    private static final String WEB_APPLICATION_ERROR = "web_application_error";
//
//    @Override
//    public Response toResponse(Throwable e) {
//        if (e instanceof WebApplicationException) {
//            WebApplicationException exception = (WebApplicationException) e;
//
//            int status = exception.getResponse().getStatus();
//            if (status == UNAUTHORIZED.getStatusCode()) {
//                logger.debug(e.getMessage(), e);
//                return createUnauthorizedWithLoginLocationResponse("Autentiseringsfeil");
//            } else if(status == FORBIDDEN.getStatusCode()) {
//                logger.debug(e.getMessage(), e);
//                return createUnauthorizedWithLoginLocationResponse( "Autoriseringsfeil");
//            } else if (e instanceof NotFoundException) {
//                logger.warn(e.getMessage(), e);
//            } else {
//                logger.error(e.getMessage(), e);
//            }
//
//            return status(exception.getResponse().getStatus()).type(APPLICATION_JSON).entity(new Feilmelding(WEB_APPLICATION_ERROR, "Noe uventet feilet")).build();
//        } else if (e instanceof SamtidigOppdateringException) {
//            logger.warn(e.getMessage(), e);
//            return status(Response.Status.CONFLICT).type(APPLICATION_JSON).entity(new Feilmelding(WEB_APPLICATION_ERROR, "Samtidig oppdatering av søknad")).build();
//        } else {
//            logger.error("Noe uventet feilet: {}", e.getMessage(), e);
//            return serverError().header(NO_BIGIP_5XX_REDIRECT, true).type(APPLICATION_JSON).entity(new Feilmelding("unexpected_error", "Noe uventet feilet")).build();
//        }
//    }
//
//    private Response createUnauthorizedWithLoginLocationResponse(String message) {
//        URI loginUrl = URI.create(System.getProperty("loginservice.url"));
//        return status(UNAUTHORIZED.getStatusCode())
//                .location(loginUrl)
//                .type(APPLICATION_JSON)
//                .entity(new UnauthorizedMelding(WEB_APPLICATION_ERROR, message, loginUrl))
//                .build();
//    }
//}
