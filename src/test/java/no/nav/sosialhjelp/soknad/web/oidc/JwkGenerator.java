package no.nav.sosialhjelp.soknad.web.oidc;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

public class JwkGenerator {
    public static String DEFAULT_KEYID = "localhost-signer";
    public static String DEFAULT_JWKSET_FILE = "/jwkset.json";

    public JwkGenerator(){}

    public static RSAKey getDefaultRSAKey(){
        return (RSAKey)getJWKSet().getKeyByKeyId(DEFAULT_KEYID);
    }

    public static JWKSet getJWKSet() {
        try {
            return JWKSet.parse(IOUtils.readInputStreamToString(JwkGenerator.class.getResourceAsStream(DEFAULT_JWKSET_FILE), Charset.forName("UTF-8")));
        } catch (IOException | ParseException io){
            throw new RuntimeException(io);
        }
    }
}
