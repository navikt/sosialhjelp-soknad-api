package no.nav.sbl.dialogarena.oidc;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.api.Unprotected;
import no.nav.security.oidc.test.support.JwtTokenGenerator;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

/** This Resource returns a cookie to the client calling it's endpoints. It can be used to simulate the NAV-microservice "LoginService" when running frontend and backend locally.
 * NB: this resource should only be used when running locally. */

@Path("/local")
public class MockLoginServiceRessurs {
    private final static int tenYears = 10*365*24*60*60;

    /** This api should only be used when running locally. */
    @Unprotected
    @Path("/cookie")
    @GET
    public Response addCookie(
            @QueryParam("subject") @DefaultValue("10108000398") String subject,
            @QueryParam("cookiename") @DefaultValue("localhost-idtoken") String cookieName,
            @QueryParam("redirect") String redirect,
            @Context HttpServletResponse response) {

        SignedJWT token = JwtTokenGenerator.createSignedJWT(subject);
        return Response.status(redirect == null ? Response.Status.OK : Response.Status.FOUND)
                .location(redirect == null ? null : URI.create(redirect))
                .cookie(new NewCookie(cookieName, token.serialize(), "/", "", "", tenYears, false))
                .build();
    }
}
