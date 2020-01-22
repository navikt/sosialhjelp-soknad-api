package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class ErrorResponseFilter implements ClientResponseFilter {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        // filter for å håndtere feil
        if (!responseContext.getStatusInfo().getFamily().equals(SUCCESSFUL)) {
            if (responseContext.hasEntity()) {
                // get the "real" error message
                ErrorResponse error = objectMapper.readValue(responseContext.getEntityStream(), ErrorResponse.class);
                String message = error.getMessage();

                Response.Status status = Response.Status.fromStatusCode(responseContext.getStatus());
                WebApplicationException webAppException;
                switch (status) {
                    case BAD_REQUEST:
                        webAppException = new BadRequestException(message);
                        break;
                    case UNAUTHORIZED:
                        webAppException = new NotAuthorizedException(message);
                        break;
                    case FORBIDDEN:
                        webAppException = new ForbiddenException(message);
                        break;
                    case NOT_FOUND:
                        webAppException = new NotFoundException(message);
                        break;
                    case METHOD_NOT_ALLOWED:
                        webAppException = new NotAllowedException(message);
                        break;
                    case NOT_ACCEPTABLE:
                        webAppException = new NotAcceptableException(message);
                        break;
                    case UNSUPPORTED_MEDIA_TYPE:
                        webAppException = new NotSupportedException(message);
                        break;
                    case INTERNAL_SERVER_ERROR:
                        webAppException = new InternalServerErrorException(message);
                        break;
                    case SERVICE_UNAVAILABLE:
                        webAppException = new ServiceUnavailableException(message);
                        break;
                    default:
                        webAppException = new WebApplicationException(message);
                }

                throw webAppException;
            }
        }
    }
}
