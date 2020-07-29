package no.nav.sbl.dialogarena.server;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import no.nav.common.oidc.auth.OidcAuthenticatorConfig;
import no.nav.common.oidc.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;

public class OidcAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(OidcAuthenticationFilter.class);

    public void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        OidcAuthenticatorConfig authenticatorConfig = new OidcAuthenticatorConfig();
        authenticatorConfig.clientId = System.getProperty("idporten_clientid");
        authenticatorConfig.discoveryUrl = System.getProperty("idporten_config_url");
        authenticatorConfig.idTokenCookieName = System.getProperty("oidc.issuer.selvbetjening.cookie_name");
        authenticatorConfig.identType = IdentType.EksternBruker;

        // Todo: Fiks config and check if token is present
        //OidcAuthenticator authenticator = OidcAuthenticator.fromConfig(authenticatorConfig);
        //Optional<String> token = authenticator.findIdToken(req);

        try {
            String token = getIdportenToken(req);
            if (token == null) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
            JWT jwtToken = JWTParser.parse(token);
            // TODO: Fiks config
            //authenticator.getTokenValidator().validate(jwtToken);
            SsoToken ssotoken = SsoToken.oidcToken(jwtToken.getParsedString(), jwtToken.getJWTClaimsSet().getClaims());
            Subject subject = new Subject(
                    TokenUtils.getUid(jwtToken, authenticatorConfig.identType),
                    authenticatorConfig.identType,
                    ssotoken);
            SubjectHandler.withSubject(subject, () -> chain.doFilter(req, res));
        } catch (ParseException /*| JOSEException | BadJOSEException*/ e) {
            logger.info("Unable to parse JWT token");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            logger.info("Generic exception", e);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        //res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private String getIdportenToken(HttpServletRequest req) {
        if (req.getCookies() == null) {
            String headerToken = req.getHeader("Authorization");
            if (headerToken == null) {
                return null;
            }
            return headerToken.substring(6);
        }
        for (Cookie cookie : req.getCookies()) {
            if (cookie.getName().equals("selvbetjening-idtoken")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
