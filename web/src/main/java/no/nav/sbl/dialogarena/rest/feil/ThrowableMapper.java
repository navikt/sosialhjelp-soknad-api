package no.nav.sbl.dialogarena.rest.feil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.serverError;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(ThrowableMapper.class);

    @Override
    public Response toResponse(Throwable e) {
        logger.error("Noe uventet feilet", e);
        return serverError().type(APPLICATION_JSON).entity(new Feilmelding("unexpected_error", e.getMessage())).build();
    }
}
