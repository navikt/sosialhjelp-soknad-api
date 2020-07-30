package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Klasse som genererer og sjekker xsrf token som sendes inn
 */
public class XsrfGenerator {
    private static final String SECRET = "9f8c0d81-d9b3-4b70-af03-bb9375336c4f";

    private static final Logger logger = LoggerFactory.getLogger(XsrfGenerator.class);

    public static String generateXsrfToken(String behandlingsId, String oidcToken) {
        return generateXsrfToken(behandlingsId, new DateTime().toString("yyyyMMdd"), oidcToken);
    }

    public static String generateXsrfToken(String behandlingsId, String date, String token) {
        try {
            String signKey = token + behandlingsId + date;
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
            hmac.init(secretKey);
            return Base64.encodeBase64URLSafeString(hmac.doFinal(signKey.getBytes()));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new SosialhjelpSoknadApiException("Kunne ikke generere token: ", e);
        }
    }

    public static void sjekkXsrfToken(String givenToken, String behandlingsId, String oidcToken) {
        String xsrfToken = generateXsrfToken(behandlingsId, oidcToken);
        logger.info("xsrf tokens are equal: " + xsrfToken.equals(givenToken));
        logger.info("xsrf token minus 1 day are equal: " + generateXsrfToken(behandlingsId, new DateTime().minusDays(1).toString("yyyyMMdd"), oidcToken).equals(givenToken));
        boolean valid = xsrfToken.equals(givenToken) || generateXsrfToken(behandlingsId, new DateTime().minusDays(1).toString("yyyyMMdd"), oidcToken).equals(givenToken);
        if (!valid && !MockUtils.isTillatMockRessurs()) {
            throw new AuthorizationException("Feil token");
        }
    }
}
