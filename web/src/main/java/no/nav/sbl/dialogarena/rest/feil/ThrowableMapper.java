package no.nav.sbl.dialogarena.rest.feil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.serverError;
import static javax.ws.rs.core.Response.status;
import static no.nav.sbl.dialogarena.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(ThrowableMapper.class);

    @Override
    public Response toResponse(Throwable e) {
        if (e instanceof WebApplicationException) {
            WebApplicationException exception = (WebApplicationException) e;

            if (e instanceof NotFoundException) {
                logger.warn(e.getMessage(), e);
            } else {
                logger.error(e.getMessage(), e);
            }

            return status(exception.getResponse().getStatus()).type(APPLICATION_JSON).entity(new Feilmelding("web_application_error", "Noe uventet feilet")).build();
        } else {
            logger.error("Noe uventet feilet", e);
            return serverError().header(NO_BIGIP_5XX_REDIRECT, true).type(APPLICATION_JSON).entity(new Feilmelding("unexpected_error", "Noe uventet feilet")).build();
        }
    }
}
