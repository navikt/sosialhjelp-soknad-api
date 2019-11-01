package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
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
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils.isTillatMockRessurs;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.KommuneStatus.*;
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


    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    @Override
    public KommuneStatus kommuneInfo(String kommunenummer, Map<String, KommuneInfo> kommuneInfoMap) {
        Map<String, KommuneInfo> stringKommuneInfoMap;
        if (cacheTimestamp.isAfter(LocalDateTime.now().minus(Duration.ofMinutes(30)))) {
            stringKommuneInfoMap = cacheForKommuneinfo.get();
        } else{
            stringKommuneInfoMap= kommuneInfoMap;
        }
        KommuneInfo kommuneInfo = stringKommuneInfoMap.getOrDefault(kommunenummer, new KommuneInfo());

        if (kommuneInfo.getKanMottaSoknader() == null) {
            return MANGLER_KONFIGURASJON;
        }

        if (!kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getKanOppdatereStatus() && !kommuneInfo.getHarMidlertidigDeaktivertMottak() && !kommuneInfo.getHarMidlertidigDeaktivertOppdateringer()) {
            return HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT;
        }

        if (kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getKanOppdatereStatus() && !kommuneInfo.getHarMidlertidigDeaktivertMottak() && !kommuneInfo.getHarMidlertidigDeaktivertOppdateringer()) {
            return SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;
        }

        if (kommuneInfo.getKanMottaSoknader() && kommuneInfo.getKanOppdatereStatus() && !kommuneInfo.getHarMidlertidigDeaktivertMottak() && !kommuneInfo.getHarMidlertidigDeaktivertOppdateringer()) {
            return SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;
        }

        if (kommuneInfo.getKanMottaSoknader() && kommuneInfo.getKanOppdatereStatus() && kommuneInfo.getHarMidlertidigDeaktivertMottak() && !kommuneInfo.getHarMidlertidigDeaktivertOppdateringer()) {
            return SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG;
        }

        if (kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getKanOppdatereStatus() && kommuneInfo.getHarMidlertidigDeaktivertMottak() && !kommuneInfo.getHarMidlertidigDeaktivertOppdateringer()) {
            return SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG;
        }

        if (kommuneInfo.getKanMottaSoknader() && kommuneInfo.getKanOppdatereStatus() && kommuneInfo.getHarMidlertidigDeaktivertMottak() && kommuneInfo.getHarMidlertidigDeaktivertOppdateringer()) {
            return SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE;
        }

        return IKKE_STOTTET_CASE;
    }

    // @Cacheable("kommuneinfoCache")
    // todo: får ikke cache til å virke, legger inn manuelt enn så lenge
    @Override
    public Map<String, KommuneInfo> hentKommuneInfo() {
        if (isTillatMockRessurs()) {
            return Collections.emptyMap();
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

            CloseableHttpResponse response = client.execute(http);
            String content = EntityUtils.toString(response.getEntity());
            log.info(content);
            Map<String, KommuneInfo> collect = Arrays.stream(objectMapper.readValue(content, KommuneInfo[].class)).collect(Collectors.toMap(KommuneInfo::getKommunenummer, Function.identity()));
            cacheForKommuneinfo.set(collect);
            cacheTimestamp =  LocalDateTime.now();
            return collect;
        } catch (Exception e) {
            log.error("Hent kommuneinfo feiler", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public String krypterOgLastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token) {
        log.info(String.format("Starter kryptering av filer, skal sende til %s %s", kommunenr, navEkseternRefId));
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
        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build();) {
            HttpUriRequest request = RequestBuilder.get().setUri(System.getProperty("digisos_api_baseurl") + "/digisos/api/v1/dokumentlager-public-key")
                    .addHeader("Accept", MediaType.WILDCARD)
                    .addHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"))
                    .addHeader("IntegrasjonPassord", System.getProperty("integrasjonpassord_fiks"))
                    .addHeader("Authorization", token).build();

            CloseableHttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 300) {
                log.warn(String.format("Statuscode ved henting av sertifikat %d token:%s", statusCode, token));
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

    private String lastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenummer, String navEkseternRefId, String token) {

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


        for (FilForOpplasting<Object> objectFilForOpplasting : filer) {
            objectFilForOpplasting.getFilnavn();
            FilMetadata metadata = (FilMetadata) objectFilForOpplasting.getMetadata();
            log.info(metadata.filnavn);
            log.info(metadata.mimetype);
            log.info("" + metadata.storrelse);
        }

        MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        entitybuilder.addTextBody("soknadJson", soknadJson);
        entitybuilder.addTextBody("vedleggJson", vedleggJson);
        for (FilForOpplasting<Object> objectFilForOpplasting : filer) {
            entitybuilder.addTextBody("metadata", getJson(objectFilForOpplasting));
            entitybuilder.addBinaryBody(objectFilForOpplasting.getFilnavn(), objectFilForOpplasting.getData(), ContentType.APPLICATION_OCTET_STREAM, objectFilForOpplasting.getFilnavn());
        }

        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build();) {
            HttpPost post = new HttpPost(System.getProperty("digisos_api_baseurl") + getLastOppFilerPath(kommunenummer, navEkseternRefId));

            post.setHeader("requestid", UUID.randomUUID().toString());
            post.setHeader("Authorization", token);
            post.setHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"));
            post.setHeader("IntegrasjonPassord", System.getProperty("integrasjonpassord_fiks"));

            post.setEntity(entitybuilder.build());
            CloseableHttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() >= 300) {
                log.warn(response.getStatusLine().getReasonPhrase());
                log.warn(EntityUtils.toString(response.getEntity()));
                throw new IllegalStateException(String.format("Opplasting feilet for %s", navEkseternRefId));
            }
            String digisosId = EntityUtils.toString(response.getEntity());
            log.info(String.format("Sendte inn søknad og fikk digisosid: %s", digisosId));
            return digisosId;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Opplasting feilet for %s", navEkseternRefId), e);
        }
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