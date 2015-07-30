package no.nav.sbl.dialogarena.rest.feil;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.VedleggGenereringMismatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.status;

@Provider
public class VedleggGenereringMismatchExceptionMapper implements ExceptionMapper<VedleggGenereringMismatch> {
    private static final Logger logger = LoggerFactory.getLogger(VedleggGenereringMismatchExceptionMapper.class);

    @Override
    public Response toResponse(VedleggGenereringMismatch vedleggGenereringMismatch) {
        logger.error(vedleggGenereringMismatch.getMessage());
        return status(Response.Status.CONFLICT).entity(vedleggGenereringMismatch.getMismatch()).build();
    }
}
