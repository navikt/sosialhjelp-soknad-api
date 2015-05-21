package no.nav.sbl.dialogarena.rest.feil;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.serverError;
import static no.nav.sbl.dialogarena.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    private static final Logger logger = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override
    public Response toResponse(JsonProcessingException e) {
        logger.error("Noe uventet feilet", e);
        return serverError().header(NO_BIGIP_5XX_REDIRECT, true).type(APPLICATION_JSON).entity(new Feilmelding("unexpected_error", "Noe uventet feilet")).build();
    }
}