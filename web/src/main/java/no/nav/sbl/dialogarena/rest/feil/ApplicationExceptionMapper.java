package no.nav.sbl.dialogarena.rest.feil;

import no.nav.modig.core.exception.AuthorizationException;
import no.nav.modig.core.exception.ModigException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.serverError;
import static javax.ws.rs.core.Response.status;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<ModigException> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionMapper.class);

    @Override
    public Response toResponse(ModigException e) {
        Response.ResponseBuilder response;
        if (e instanceof UgyldigOpplastingTypeException) {
            response = status(UNSUPPORTED_MEDIA_TYPE);
            logger.warn("Feilet opplasting", e);
        } else if (e instanceof OpplastingException) {
            response = status(NOT_ACCEPTABLE);
            logger.warn("Feilet opplasting", e);
        } else if (e instanceof AuthorizationException) {
            response = status(FORBIDDEN);
            logger.warn("Ikke tilgang til ressurs", e);
            return response.type(APPLICATION_JSON).entity(new Feilmelding(e.getId(), "Ikke tilgang til ressurs")).build();
        } else {
            response = serverError();
            logger.error("REST-kall feilet", e);
        }

        return response.type(APPLICATION_JSON).entity(new Feilmelding(e.getId(), e.getMessage())).build();
    }
}
