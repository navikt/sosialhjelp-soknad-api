package no.nav.sosialhjelp.soknad.web.oidc;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader.Builder;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.UUID;

public class JwtTokenGenerator {

    public static String ISS = "iss-localhost";
    public static String ISS_TOKENX = "iss-localhost-tokenx";
    public static String AUD = "aud-localhost";
    public static String ACR = "Level4";
    public static int EXPIRY = 60 * 60 * 3600;

    private JwtTokenGenerator() {
    }

    public static SignedJWT createSignedJWT(String subject) {
        JWTClaimsSet claimsSet = buildClaimSet(subject, ISS, AUD, ACR, EXPIRY, null);
        return createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet);
    }

    public static SignedJWT createSignedJwtForTokenx(String subject, String uniqueName) {
        JWTClaimsSet claimsSet = buildClaimSet(subject, ISS_TOKENX, AUD, ACR, EXPIRY, uniqueName);
        return createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet);
    }

    public static SignedJWT createSignedJWT(JWTClaimsSet claimsSet) {
        return createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet);
    }

    public static JWTClaimsSet buildClaimSet(String subject, String issuer, String audience, String authLevel,
                                             int expiry, String uniqueName) {
        Date now = new Date();
        return new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .audience(audience)
                .jwtID(UUID.randomUUID().toString())
                .claim("acr", authLevel)
                .claim("ver", "1.0")
                .claim("nonce", "myNonce")
                .claim("auth_time", now)
                .claim("unique_name", uniqueName)
                .notBeforeTime(now)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + expiry)).build();
    }

    protected static SignedJWT createSignedJWT(RSAKey rsaJwk, JWTClaimsSet claimsSet) {
        try {
            Builder header = new Builder(JWSAlgorithm.RS256)
                    .keyID(rsaJwk.getKeyID())
                    .type(JOSEObjectType.JWT);

            SignedJWT signedJWT = new SignedJWT(header.build(), claimsSet);
            JWSSigner signer = new RSASSASigner(rsaJwk.toPrivateKey());
            signedJWT.sign(signer);

            return signedJWT;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
