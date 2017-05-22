package no.nav.sbl.dialogarena.rest.feil;

import no.nav.modig.core.exception.AuthorizationException;
import no.nav.modig.core.exception.ModigException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.IkkeFunnetException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.serverError;
import static javax.ws.rs.core.Response.status;
import static no.nav.sbl.dialogarena.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;

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
            response = status(REQUEST_ENTITY_TOO_LARGE);
            logger.warn("Feilet opplasting", e);
        } else if (e instanceof AuthorizationException) {
            response = status(FORBIDDEN);
            logger.warn("Ikke tilgang til ressurs", e);
            return response.type(APPLICATION_JSON).entity(new Feilmelding(e.getId(), "Ikke tilgang til ressurs")).build();
        } else if (e instanceof IkkeFunnetException) {
            response = status(NOT_FOUND);
            logger.warn("Fant ikke ressurs", e);
        } else if (e instanceof AlleredeHandtertException) {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
        } else {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
            logger.error("REST-kall feilet", e);
        }

        // Mediatypen kan settes til APPLICATION_JSON når vi ikke trenger å støtte IE9 lenger.
        return response.type(TEXT_PLAIN).entity(new Feilmelding(e.getId(), e.getMessage())).build();
    }
}
