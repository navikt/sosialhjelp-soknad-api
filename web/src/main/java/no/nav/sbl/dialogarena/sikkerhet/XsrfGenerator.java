package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.AuthorizationException;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Klasse som genererer og sjekker xsrf token som sendes inn
 */
public class XsrfGenerator {
    private static final String SECRET = "9f8c0d81-d9b3-4b70-af03-bb9375336c4f";

    public static String generateXsrfToken(String behandlingsId) {
        return generateXsrfToken(behandlingsId, new DateTime().toString("yyyyMMdd"));
    }

    public static String generateXsrfToken(String behandlingsId, String date) {
        try {
            String signKey = SubjectHandler.getSubjectHandler().getEksternSsoToken() + behandlingsId + date;
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
            hmac.init(secretKey);
            return Base64.encodeBase64URLSafeString(hmac.doFinal(signKey.getBytes()));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ApplicationException("Kunne ikke generere token: ", e);
        }
    }

    public static void sjekkXsrfToken(String givenToken, String behandlingsId) {
        String token = generateXsrfToken(behandlingsId);
        boolean valid = token.equals(givenToken) || generateXsrfToken(behandlingsId, new DateTime().minusDays(1).toString("yyyyMMdd")).equals(givenToken);
        if (!valid) {
            throw new AuthorizationException("Feil token");
        }
    }
}
