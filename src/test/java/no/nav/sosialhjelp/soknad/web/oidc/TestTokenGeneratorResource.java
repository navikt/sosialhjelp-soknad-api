package no.nav.sosialhjelp.soknad.web.oidc;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

/** This Resource should only be used when running locally. */
@Controller
@Path("local")
public class TestTokenGeneratorResource {
    private final static int tenYears = 10*365*24*60*60;

    /** This api should only be used when running locally. */
    @Unprotected
    @Path("cookie")
    @GET
    public Response addCookie(
            @QueryParam("subject") @DefaultValue("12345678910") String subject,
            @QueryParam("cookiename") @DefaultValue("localhost-idtoken") String cookieName,
            @QueryParam("redirect") String redirect,
            @Context HttpServletResponse response) {

        SignedJWT token = JwtTokenGenerator.createSignedJWT(subject);
        return Response.status(redirect == null ? Response.Status.OK : Response.Status.FOUND)
                .location(redirect == null ? null : URI.create(redirect))
                .cookie(new NewCookie(cookieName, token.serialize(), "/", "localhost", "", tenYears, false))
                .build();
    }

    /** This api should only be used when running locally. */
    @Unprotected
    @Path("cookie-tokenx")
    @GET
    public Response addCookie(
            @QueryParam("subject") @DefaultValue("12345678910") String subject,
            @QueryParam("uniqueName") @DefaultValue("test@nav.no") String uniqueName,
            @QueryParam("cookiename") @DefaultValue("localhost-idtoken-tokenx") String cookieName,
            @QueryParam("redirect_uri") String redirect,
            @Context HttpServletResponse response) {

        SignedJWT token = JwtTokenGenerator.createSignedJwtForTokenx(subject, uniqueName);
        return Response.status(redirect == null ? Response.Status.OK : Response.Status.FOUND)
                .location(redirect == null ? null : URI.create(redirect))
                .cookie(new NewCookie(cookieName, token.serialize(), "/", "localhost", "", tenYears, false))
                .build();
    }
}