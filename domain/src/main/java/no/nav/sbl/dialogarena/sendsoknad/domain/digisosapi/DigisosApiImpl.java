package no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.ks.fiks.streaming.klient.FilForOpplasting;
import no.ks.kryptering.CMSKrypteringImpl;
import no.ks.kryptering.CMSStreamKryptering;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URI;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils.isTillatMockRessurs;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.stripVekkFnutter;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DigisosApiImpl implements DigisosApi {

    private static final Logger log = getLogger(DigisosApiImpl.class);
    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();
    private ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(Executors.newCachedThreadPool());
    private String idPortenTokenUrl;
    private String idPortenClientId;
    private String idPortenScope;
    private IdPortenOidcConfiguration idPortenOidcConfiguration;
    private AtomicReference<Map<String, KommuneInfo>> cacheForKommuneinfo = new AtomicReference<>(Collections.emptyMap());
    private LocalDateTime cacheTimestamp = LocalDateTime.MIN;
    private static final long KOMMUNEINFO_CACHE_IN_MINUTES = 1;
    private static final int SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000; // 5 minutter

    public DigisosApiImpl() {
        if (MockUtils.isTillatMockRessurs()) {
            return;
        }
        this.idPortenTokenUrl = System.getProperty("idporten_token_url");
        this.idPortenClientId = System.getProperty("idporten_clientid");
        this.idPortenScope = System.getProperty("idporten_scope");
        try {
            idPortenOidcConfiguration = objectMapper.readValue(URI.create(System.getProperty("idporten_config_url")).toURL(), IdPortenOidcConfiguration.class);
        } catch (IOException e) {
            log.warn("", e);
        }
    }

    @Override
    public void ping() {
        Map<String, KommuneInfo> kommuneInfo = hentKommuneInfo();
        if (kommuneInfo.isEmpty()) {
            throw new IllegalStateException("Fikk ikke kontakt med digisosapi");
        }
    }

    // @Cacheable("kommuneinfoCache")
    // todo: får ikke cache til å virke, legger inn manuelt enn så lenge
    @Override
    public Map<String, KommuneInfo> hentKommuneInfo() {
        if (isTillatMockRessurs()) {
            return Collections.emptyMap();
        }

        if (cacheTimestamp.isAfter(LocalDateTime.now().minus(Duration.ofMinutes(KOMMUNEINFO_CACHE_IN_MINUTES)))) {
            return cacheForKommuneinfo.get();
        }

        IdPortenAccessTokenResponse accessToken = getVirksertAccessToken();
        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet http = new HttpGet(System.getProperty("digisos_api_baseurl") + "digisos/api/v1/nav/kommuner/");
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"));
            String integrasjonpassord_fiks = System.getProperty("integrasjonpassord_fiks");
            Objects.requireNonNull(integrasjonpassord_fiks, "integrasjonpassord_fiks");
            http.setHeader("IntegrasjonPassord", integrasjonpassord_fiks);
            http.setHeader("Authorization", "Bearer " + accessToken.accessToken);

            long startTime = System.currentTimeMillis();
            CloseableHttpResponse response = client.execute(http);
            long endTime = System.currentTimeMillis();
            if (endTime - startTime > 8000) {
                log.error("Timer: Sende fiks-request: {} ms", endTime - startTime);
            } else if (endTime - startTime > 2000) {
                log.warn("Timer: Sende fiks-request: {} ms", endTime - startTime);
            }

            String content = EntityUtils.toString(response.getEntity());
            log.info("KommuneInfo: {}", content);
            Map<String, KommuneInfo> collect = Arrays.stream(objectMapper.readValue(content, KommuneInfo[].class)).collect(Collectors.toMap(KommuneInfo::getKommunenummer, Function.identity()));
            cacheForKommuneinfo.set(collect);
            cacheTimestamp = LocalDateTime.now();
            return collect;
        } catch (Exception e) {
            if(cacheForKommuneinfo.get().isEmpty()) {
                log.error("Hent kommuneinfo feiler og cache er tom!", e);
                return Collections.emptyMap();
            }
            log.error("Hent kommuneinfo feiler og cache er gammel.", e);
            return cacheForKommuneinfo.get();
        }
    }

    @Override
    public String krypterOgLastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token) {
        log.info("Starter kryptering av filer, skal sende til {} {}", kommunenr, navEkseternRefId);
        List<Future<Void>> krypteringFutureList = Collections.synchronizedList(new ArrayList<>(dokumenter.size()));
        String digisosId;
        try {
            X509Certificate dokumentlagerPublicKeyX509Certificate = getDokumentlagerPublicKeyX509Certificate(token);
            digisosId = lastOppFiler(soknadJson, vedleggJson, dokumenter.stream()
                    .map(dokument -> new FilOpplasting(dokument.metadata, krypter(dokument.data, krypteringFutureList, dokumentlagerPublicKeyX509Certificate)))
                    .collect(Collectors.toList()), kommunenr, navEkseternRefId, token);

            waitForFutures(krypteringFutureList);

        } finally {
            krypteringFutureList.stream().filter(f -> !f.isDone() && !f.isCancelled()).forEach(future -> future.cancel(true));
        }
        return digisosId;
    }

    private X509Certificate getDokumentlagerPublicKeyX509Certificate(String token) {
        byte[] publicKey = new byte[0];
        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpUriRequest request = RequestBuilder.get().setUri(System.getProperty("digisos_api_baseurl") + "/digisos/api/v1/dokumentlager-public-key")
                    .addHeader("Accept", MediaType.WILDCARD)
                    .addHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"))
                    .addHeader("IntegrasjonPassord", System.getProperty("integrasjonpassord_fiks"))
                    .addHeader("Authorization", token).build();

            CloseableHttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 300) {
                log.warn("Statuscode ved henting av sertifikat {} token:{}", statusCode, token);
                log.warn(response.getStatusLine().getReasonPhrase());
                log.warn(EntityUtils.toString(response.getEntity()));
            }
            publicKey = IOUtils.toByteArray(response.getEntity().getContent());
        } catch (IOException e) {
            log.error("", e);
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(publicKey));

        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream krypter(InputStream dokumentStream, List<Future<Void>> krypteringFutureList, X509Certificate dokumentlagerPublicKeyX509Certificate) {
        CMSStreamKryptering kryptering = new CMSKrypteringImpl();

        PipedInputStream pipedInputStream = new PipedInputStream();
        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            Future<Void> krypteringFuture =
                    executor.submit(() -> {
                        try {
                            kryptering.krypterData(pipedOutputStream, dokumentStream, dokumentlagerPublicKeyX509Certificate, Security.getProvider("BC"));
                        } catch (Exception e) {
                            log.error("Encryption failed, setting exception on encrypted InputStream", e);
                            throw new IllegalStateException("An error occurred during encryption", e);
                        } finally {
                            try {
                                log.debug("Closing encryption OutputStream");
                                pipedOutputStream.close();
                                log.debug("Encryption OutputStream closed");
                            } catch (IOException e) {
                                log.error("Failed closing encryption OutputStream", e);
                            }
                        }
                        return null;
                    });
            krypteringFutureList.add(krypteringFuture);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pipedInputStream;
    }

    private void waitForFutures(List<Future<Void>> krypteringFutureList) {
        for (Future<Void> voidFuture : krypteringFutureList) {
            try {
                voidFuture.get(300, TimeUnit.SECONDS);
            } catch (CompletionException e) {
                throw new IllegalStateException(e.getCause());
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private String lastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenummer, String behandlingsId, String token) {

        List<FilForOpplasting<Object>> filer = new ArrayList<>();

        dokumenter.forEach(dokument -> filer.add(FilForOpplasting.builder()
                .filnavn(dokument.metadata.filnavn)
                .metadata(new FilMetadata()
                        .withFilnavn(dokument.metadata.filnavn)
                        .withMimetype(dokument.metadata.mimetype)
                        .withStorrelse(dokument.metadata.storrelse)
                )
                .data(dokument.data)
                .build()));

        MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();
        entitybuilder.setCharset(Charsets.UTF_8);
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        entitybuilder.addTextBody("soknadJson", soknadJson, ContentType.APPLICATION_JSON);
        entitybuilder.addTextBody("vedleggJson", vedleggJson, ContentType.APPLICATION_JSON);
        for (FilForOpplasting<Object> objectFilForOpplasting : filer) {
            entitybuilder.addTextBody("metadata", getJson(objectFilForOpplasting));
            entitybuilder.addBinaryBody(objectFilForOpplasting.getFilnavn(), objectFilForOpplasting.getData(), ContentType.APPLICATION_OCTET_STREAM, objectFilForOpplasting.getFilnavn());
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(SENDING_TIL_FIKS_TIMEOUT)
                .setConnectionRequestTimeout(SENDING_TIL_FIKS_TIMEOUT)
                .setSocketTimeout(SENDING_TIL_FIKS_TIMEOUT)
                .build();

        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().setDefaultRequestConfig(requestConfig).build()) {
            HttpPost post = new HttpPost(System.getProperty("digisos_api_baseurl") + getLastOppFilerPath(kommunenummer, behandlingsId));

            post.setHeader("requestid", UUID.randomUUID().toString());
            post.setHeader("Authorization", token);
            post.setHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"));
            post.setHeader("IntegrasjonPassord", System.getProperty("integrasjonpassord_fiks"));

            post.setEntity(entitybuilder.build());
            CloseableHttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() >= 300) {
                String errorResponse = EntityUtils.toString(response.getEntity());
                String fiksDigisosId = getDigisosIdFromResponse(errorResponse, behandlingsId);
                if (fiksDigisosId != null) {
                    log.error("Søknad {} er allerede sendt til fiks-digisos-api med id {}. Ruter brukeren til innsynssiden. ErrorResponse var: {} ", behandlingsId, fiksDigisosId, errorResponse);
                    return fiksDigisosId;
                }

                throw new IllegalStateException(String.format("Opplasting av %s til fiks-digisos-api feilet med status %s og response: %s",
                        behandlingsId,
                        response.getStatusLine().getReasonPhrase(),
                        errorResponse));
            }
            String digisosId = stripVekkFnutter(EntityUtils.toString(response.getEntity()));
            log.info("Sendte inn søknad og fikk digisosid: {}", digisosId);
            return digisosId;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Opplasting av %s til fiks-digisos-api feilet", behandlingsId), e);
        }
    }

    static String getDigisosIdFromResponse(String errorResponse, String behandlingsId) {
        if (errorResponse != null && errorResponse.contains(behandlingsId) && errorResponse.contains("finnes allerede")) {
            Pattern p = Pattern.compile("^.*?message.*([0-9a-fA-F]{8}[-]?(?:[0-9a-fA-F]{4}[-]?){3}[0-9a-fA-F]{12}).*?$");
            Matcher m = p.matcher(errorResponse);
            if (m.matches()) {
                return m.group(1);
            }
        }
        return null;
    }

    private String getJson(FilForOpplasting<Object> objectFilForOpplasting) {
        try {
            return objectMapper.writeValueAsString(objectFilForOpplasting.getMetadata());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getLastOppFilerPath(String kommunenummer, String navEkseternRefId) {
        return String.format("/digisos/api/v1/soknader/%s/%s", kommunenummer, navEkseternRefId);
    }

    private IdPortenAccessTokenResponse getVirksertAccessToken() {
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
            throw new IllegalStateException("Far ikke tak i virksomhets accessToken", e);
        }
    }

    private String createJws() {
        try {
            String virksomhetsSertifikatPath = System.getProperty("virksomhetssertifikat_path", "/var/run/secrets/nais.io/virksomhetssertifikat");
            VirksertCredentials virksertCredentials = objectMapper.readValue(FileUtils.readFileToString(new File(virksomhetsSertifikatPath + "/credentials.json")), VirksertCredentials.class);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            String src = FileUtils.readFileToString(new File(virksomhetsSertifikatPath + "/key.p12.b64"));
            keyStore.load(new ByteArrayInputStream(Base64.getDecoder().decode(src)), virksertCredentials.password.toCharArray());

            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(virksertCredentials.alias);

            KeyPair keyPair = new KeyPair(certificate.getPublicKey(), (PrivateKey) keyStore.getKey(virksertCredentials.alias, virksertCredentials.password.toCharArray()));

            Date date = new Date();
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            instance.add(Calendar.SECOND, 100);
            Date expDate = instance.getTime();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(Collections.singletonList((com.nimbusds.jose.util.Base64.encode(certificate.getEncoded())))).build(),
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
            throw new IllegalStateException("Far ikke tak i jws token", e);
        }
    }

    @SuppressWarnings("WeakerAccess")
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IdPortenAccessTokenResponse {
        @JsonProperty(value = "access_token", required = true)
        String accessToken;
        @JsonProperty(value = "expires_in", required = true)
        Integer expiresIn;
        @JsonProperty(value = "scope", required = true)
        String scope;
    }
}