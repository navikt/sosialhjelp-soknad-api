package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static no.nav.sbl.sosialhjelp.pdf.UrlUtils.getEttersendelseUrl;
import static no.nav.sbl.sosialhjelp.pdf.UrlUtils.getFortsettUrl;

@Path("/")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
public class RedirectRessurs {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectRessurs.class);

    @GET
    @Path("utslagskriterier/{behandlingsId}")
    public Response fortsettSenereRedirect(@PathParam("behandlingsId") String behandlingsId) throws URISyntaxException {
        LOG.info("Fortsett senere med gammel URL");
        return movedPermanently(new URI(getFortsettUrl(behandlingsId)));
    }

    @GET
    @Path("startettersending/{behandlingsId}")
    public Response ettersendRedirect(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletRequest request) throws URISyntaxException {
        LOG.info("Ettersend med gammel URL");
        return movedPermanently(new URI(getEttersendelseUrl(request.getRequestURL().toString(), behandlingsId)));
    }

    private Response movedPermanently(URI uri) {
        return Response.status(MOVED_PERMANENTLY).location(uri).build();
    }
}
