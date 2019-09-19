package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Component
public class IdPortenService {
    private final String idPortenTokenUrl;
    private final String idPortenClientId;
    private final String idPortenScope;
    private final Logger log = LoggerFactory.getLogger(IdPortenService.class);
    private IdPortenOidcConfiguration idPortenOidcConfiguration;
    private ObjectMapper objectMapper;

    public IdPortenService() {
        this.idPortenTokenUrl = System.getProperty("idporten_token_url");
        this.idPortenClientId = System.getProperty("idporten_clientid");
        this.idPortenScope = System.getProperty("idporten_scope");
        try {
            objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();
            idPortenOidcConfiguration = objectMapper.readValue(URI.create(System.getProperty("idporten_config_url")).toURL(), IdPortenOidcConfiguration.class);
        } catch (IOException e) {
            log.warn("", e);
        }
    }

    public IdPortenAccessTokenResponse getVirksertAccessToken() {
        String jws = createJws();
        HttpPost httpPost = new HttpPost(idPortenTokenUrl);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
        params.add(new BasicNameValuePair("assertion", jws));

        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {

            httpPost.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpResponse response = client.execute(httpPost);

            return objectMapper.readValue(EntityUtils.toString(response.getEntity()), IdPortenAccessTokenResponse.class);
        } catch (IOException e) {
            log.warn("", e);
        }
        return null;
    }

    private String createJws() {
        try {
            VirksertCredentials virksertCredentials = objectMapper.readValue(FileUtils.readFileToString(new File("/var/run/secrets/nais.io/virksomhetssertifikat/credentials.json")), VirksertCredentials.class);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(Base64.getDecoder().decode(FileUtils.readFileToString(new File("/var/run/secrets/nais.io/virksomhetssertifikat/key.p12.b64")))), virksertCredentials.password.toCharArray());

            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(virksertCredentials.alias);

            KeyPair keyPair = new KeyPair(certificate.getPublicKey(), (PrivateKey) keyStore.getKey(virksertCredentials.alias, virksertCredentials.password.toCharArray()));
            byte[] encoded = certificate.getEncoded();


            Date date = new Date();
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            instance.add(Calendar.SECOND, 100);
            Date expDate = instance.getTime();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(Collections.singletonList((com.nimbusds.jose.util.Base64.encode(encoded)))).build(),
                    new JWTClaimsSet.Builder()
                            .audience(idPortenOidcConfiguration.issuer)
                            .issuer(idPortenClientId)
                            .issueTime(date)
                            .jwtID(UUID.randomUUID().toString())
                            .expirationTime(expDate)
                            .claim("scope", idPortenScope)
                            .build());
            signedJWT.sign(new RSASSASigner(keyPair.getPrivate()));
            return signedJWT.serialize();

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | JOSEException e) {
            log.warn("", e);
        }
        return "";
    }

    public static class AcceessToken {
        public String token;
    }

    static class VirksertCredentials {
        public String alias;
        public String password;
        public String type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IdPortenOidcConfiguration {
        @JsonProperty(value = "issuer", required = true)
        String issuer;
        @JsonProperty(value = "token_endpoint", required = true)
        String tokenEndpoint;
    }

    static class IdPortenAccessTokenResponse {
        @JsonProperty(value = "access_token", required = true)
        String accessToken;
        @JsonProperty(value = "expires_in", required = true)
        Integer expiresIn;
        @JsonProperty(value = "scope", required = true)
        String scope;
    }

}
