package no.nav.sbl.dialogarena.rest.ressurser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getEttersendelseUrl;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getFortsettUrl;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getStartDagpengerUrl;

@Path("/")
public class RedirectRessurs {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectRessurs.class);

    /*
     * TODO: Fjern denne pathen når url på "Send elektronisk"-knappen på
     * <nav.no-domene>/no/Person/Skjemaer-for-privatpersoner/Skjemaer/Uten+arbeid/Dagpenger
     * er oppdatert og det ikke kommer flere innslag av treff på ressurssen i loggene.
     */
    @GET
    @Path("utslagskriterier/dagpenger")
    public Response startNyDagpenger() throws URISyntaxException {
        LOG.info("Start ny dagpengesøknad fra gammel url-inngang fra nav.no");
        return movedPermanently(new URI(getStartDagpengerUrl()));
    }

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
