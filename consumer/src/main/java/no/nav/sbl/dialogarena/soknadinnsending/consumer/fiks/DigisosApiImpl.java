package no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.ks.fiks.streaming.klient.FilForOpplasting;
import no.ks.kryptering.CMSKrypteringImpl;
import no.ks.kryptering.CMSStreamKryptering;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.FilMetadata;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.FilOpplasting;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils.isTillatMockRessurs;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_INTEGRASJON_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_INTEGRASJON_PASSORD;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.stripVekkFnutter;
import static org.eclipse.jetty.http.HttpHeader.ACCEPT;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DigisosApiImpl implements DigisosApi {

    private static final Logger log = getLogger(DigisosApiImpl.class);
    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper
            .createObjectMapper()
            .registerModule(new KotlinModule());
    private ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(Executors.newCachedThreadPool());

    @Value("${idporten_token_url}")
    private String idPortenTokenUrl;

    @Value("${idporten_clientid}")
    private String idPortenClientId;

    @Value("${idporten_scope}")
    private String idPortenScope;

    @Value("${idporten_config_url}")
    private String idPortenConfigUrl;

    @Value("${integrasjonsid_fiks}")
    private String integrasjonsidFiks;

    @Value("${integrasjonpassord_fiks}")
    private String integrasjonpassordFiks;

    private IdPortenOidcConfiguration idPortenOidcConfiguration;
    private AtomicReference<Map<String, KommuneInfo>> cacheForKommuneinfo = new AtomicReference<>(Collections.emptyMap());
    private LocalDateTime cacheTimestamp = LocalDateTime.MIN;
    private static final long KOMMUNEINFO_CACHE_IN_MINUTES = 1;
    private static final int SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000; // 5 minutter
    private byte[] fiksPublicKey = null;

    private String endpoint;

    @Inject
    public DigisosApiImpl(String endpoint) {
        if (MockUtils.isTillatMockRessurs()) {
            return;
        }

        try {
            idPortenOidcConfiguration = objectMapper.readValue(URI.create(idPortenConfigUrl).toURL(), IdPortenOidcConfiguration.class);
        } catch (IOException e) {
            log.error(String.format("Henting av idportens konfigurasjon feilet. idPortenConfigUrl=%s", idPortenConfigUrl), e);
        }
        this.endpoint = endpoint;
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
            HttpGet http = new HttpGet(endpoint + "digisos/api/v1/nav/kommuner/");
            http.setHeader(ACCEPT.name(), MediaType.APPLICATION_JSON);
            http.setHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks);
            Objects.requireNonNull(integrasjonpassordFiks, "integrasjonpassordFiks");
            http.setHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks);
            http.setHeader(AUTHORIZATION.name(), "Bearer " + accessToken.accessToken);

            long startTime = System.currentTimeMillis();
            CloseableHttpResponse response = client.execute(http);
            long endTime = System.currentTimeMillis();
            if (endTime - startTime > 8000) {
                log.error("Timer: Sende fiks-request: {} ms", endTime - startTime);
            } else if (endTime - startTime > 2000) {
                log.warn("Timer: Sende fiks-request: {} ms", endTime - startTime);
            }

            String content = EntityUtils.toString(response.getEntity());

            Map<String, KommuneInfo> collect = Arrays.stream(objectMapper.readValue(content, KommuneInfo[].class)).collect(Collectors.toMap(KommuneInfo::getKommunenummer, Function.identity()));
            cacheForKommuneinfo.set(collect);
            logKommuneInfoForInnsynskommuner(collect);
            cacheTimestamp = LocalDateTime.now();
            return collect;
        } catch (Exception e) {
            if (cacheForKommuneinfo.get().isEmpty()) {
                log.error("Hent kommuneinfo feiler og cache er tom!", e);
                return Collections.emptyMap();
            }
            log.error("Hent kommuneinfo feiler og cache er gammel.", e);
            return cacheForKommuneinfo.get();
        }
    }

    private void logKommuneInfoForInnsynskommuner(Map<String, KommuneInfo> kommuneInfo) {
        Map<String, KommuneInfo> kommunerMedInnsyn = kommuneInfo.entrySet()
                .stream()
                .filter(kommune -> kommune.getValue().getKanOppdatereStatus())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("KommuneInfo for kommuner med innsyn aktivert: {}", kommunerMedInnsyn.toString());
    }

    @Override
    public String krypterOgLastOppFiler(String soknadJson, String tilleggsinformasjonJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String behandlingsId, String token) {
        List<Future<Void>> krypteringFutureList = Collections.synchronizedList(new ArrayList<>(dokumenter.size()));
        String digisosId;
        try {
            X509Certificate fiksX509Certificate = getFiksPublicKeyX509Certificate(token);
            digisosId = lastOppFiler(soknadJson, tilleggsinformasjonJson, vedleggJson, dokumenter.stream()
                    .map(dokument -> new FilOpplasting(dokument.metadata, krypter(dokument.data, krypteringFutureList, fiksX509Certificate)))
                    .collect(Collectors.toList()), kommunenr, behandlingsId, token);

            waitForFutures(krypteringFutureList);

        } finally {
            krypteringFutureList.stream().filter(f -> !f.isDone() && !f.isCancelled()).forEach(future -> future.cancel(true));
        }
        return digisosId;
    }

    private X509Certificate getFiksPublicKeyX509Certificate(String token) {
        fetchFiksPublicKeyIfNull(token);
        return generateX509CertificateFromFiksPublicKey();
    }

    private void fetchFiksPublicKeyIfNull(String token) {
        // Fiks public key skal aldri endres. Isåfall vil Fiks gi en tydelig beskjed.
        //Denne integrasjonen kan feile så fiksPublicKey blir derfor cachet.
        if (fiksPublicKey == null) {
            try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
                HttpUriRequest request = RequestBuilder.get().setUri(endpoint + "/digisos/api/v1/dokumentlager-public-key")
                        .addHeader(ACCEPT.name(), MediaType.WILDCARD)
                        .addHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
                        .addHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
                        .addHeader(AUTHORIZATION.name(), token).build();

                CloseableHttpResponse response = client.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 300) {
                    log.error("Statuscode ved henting av sertifikat {} - {}, response:{}",
                            statusCode,
                            response.getStatusLine().getReasonPhrase(),
                            EntityUtils.toString(response.getEntity()));
                }
                fiksPublicKey = IOUtils.toByteArray(response.getEntity().getContent());
            } catch (IOException e) {
                log.error("Henting av FIKS publicKey feilet.", e);
            }
        }
    }

    private X509Certificate generateX509CertificateFromFiksPublicKey() {
        try {
            return (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(fiksPublicKey));

        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream krypter(InputStream dokumentStream, List<Future<Void>> krypteringFutureList, X509Certificate fiksX509Certificate) {
        CMSStreamKryptering kryptering = new CMSKrypteringImpl();

        PipedInputStream pipedInputStream = new PipedInputStream();
        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            Future<Void> krypteringFuture =
                    executor.submit(() -> {
                        try {
                            kryptering.krypterData(pipedOutputStream, dokumentStream, fiksX509Certificate, Security.getProvider("BC"));
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

    private String lastOppFiler(String soknadJson, String tilleggsinformasjonJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenummer, String behandlingsId, String token) {

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
        entitybuilder.setCharset(StandardCharsets.UTF_8);
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        entitybuilder.addTextBody("tilleggsinformasjonJson", tilleggsinformasjonJson, ContentType.APPLICATION_JSON); // Må være første fil
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
            HttpPost post = new HttpPost(endpoint + getLastOppFilerPath(kommunenummer, behandlingsId));

            post.setHeader("requestid", UUID.randomUUID().toString());
            post.setHeader(AUTHORIZATION.name(), token);
            post.setHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks);
            post.setHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks);

            post.setEntity(entitybuilder.build());
            long startTime = System.currentTimeMillis();
            CloseableHttpResponse response = client.execute(post);
            long endTime = System.currentTimeMillis();
            if (response.getStatusLine().getStatusCode() >= 300) {
                String errorResponse = EntityUtils.toString(response.getEntity());
                String fiksDigisosId = getDigisosIdFromResponse(errorResponse, behandlingsId);
                if (fiksDigisosId != null) {
                    log.warn("Søknad {} er allerede sendt til fiks-digisos-api med id {}. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: {} ", behandlingsId, fiksDigisosId, errorResponse);
                    return fiksDigisosId;
                }

                throw new IllegalStateException(String.format("Opplasting av %s til fiks-digisos-api feilet etter %s ms med status %s og response: %s",
                        behandlingsId,
                        endTime - startTime,
                        response.getStatusLine().getReasonPhrase(),
                        errorResponse));
            }
            String digisosId = stripVekkFnutter(EntityUtils.toString(response.getEntity()));
            log.info("Sendte inn søknad {} og fikk digisosid: {}", behandlingsId, digisosId);
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
            VirksertCredentials virksertCredentials = objectMapper.readValue(FileUtils.readFileToString(new File(virksomhetsSertifikatPath + "/credentials.json"), StandardCharsets.UTF_8), VirksertCredentials.class);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            String src = FileUtils.readFileToString(new File(virksomhetsSertifikatPath + "/key.p12.b64"), StandardCharsets.UTF_8);
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