package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.fiks.streaming.klient.FilForOpplasting;
import no.ks.kryptering.CMSKrypteringImpl;
import no.ks.kryptering.CMSStreamKryptering;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils.isTillatMockRessurs;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.KommuneStatus.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DigisosApiImpl implements DigisosApi {

    private static final Logger log = getLogger(DigisosApiImpl.class);
    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();
    private ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(Executors.newCachedThreadPool());

    @Override
    public void ping() {
        KommuneInfo kommuneInfo = hentKommuneInfo("0301");
        if (kommuneInfo.getKanMottaSoknader() == null) {
            throw new IllegalStateException("Fikk ikke kontakt med digisosapi");
        }
    }

    @Inject
    private IdPortenService idPortenService;

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    @Override
    public KommuneStatus kommuneInfo(String kommunenummer) {
        KommuneInfo kommuneInfo = hentKommuneInfo(kommunenummer);

        if (kommuneInfo.getKanMottaSoknader() == null) {
            return IKKE_PA_FIKS_ELLER_INNSYN;
        }

        if (!kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getKanOppdatereStatus()) {
            return IKKE_PA_FIKS_ELLER_INNSYN;
        }
        if (kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getKanOppdatereStatus()) {
            return KUN_PA_FIKS;
        }
        if (kommuneInfo.getKanMottaSoknader() && kommuneInfo.getKanOppdatereStatus()) {
            return PA_FIKS_OG_INNSYN;
        }
        return IKKE_PA_FIKS_ELLER_INNSYN;
    }

    private KommuneInfo hentKommuneInfo(String kommunenummer) {
        if (isTillatMockRessurs()) {
            return new KommuneInfo();
        }
        IdPortenService.IdPortenAccessTokenResponse accessToken = idPortenService.getVirksertAccessToken();
        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet http = new HttpGet(System.getProperty("digisos_api_baseurl") + "digisos/api/v1/nav/kommune/" + kommunenummer);
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"));
            String integrasjonpassord_fiks = System.getProperty("integrasjonpassord_fiks");
            Objects.requireNonNull(integrasjonpassord_fiks, "integrasjonpassord_fiks");
            http.setHeader("IntegrasjonPassord", integrasjonpassord_fiks);
            http.setHeader("Authorization", "Bearer " + accessToken.accessToken);

            CloseableHttpResponse response = client.execute(http);
            String content = EntityUtils.toString(response.getEntity());
            log.info(content);
            return objectMapper.readValue(content, KommuneInfo.class);
        } catch (Exception e) {
            log.error("Hent kommuneinfo feiler", e);
            return new KommuneInfo();
        }
    }

    @Override
    public void krypterOgLastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token) {
        log.info(String.format("Starter kryptering av filer, skal sende til %s %s", kommunenr, navEkseternRefId));
        List<Future<Void>> krypteringFutureList = Collections.synchronizedList(new ArrayList<>(dokumenter.size()));
        try {
            X509Certificate dokumentlagerPublicKeyX509Certificate = getDokumentlagerPublicKeyX509Certificate(token);
            lastOppFiler(soknadJson, vedleggJson, dokumenter.stream()
                    .map(dokument -> new FilOpplasting(dokument.metadata, krypter(dokument.data, krypteringFutureList, dokumentlagerPublicKeyX509Certificate)))
                    .collect(Collectors.toList()), kommunenr, navEkseternRefId, token);

            waitForFutures(krypteringFutureList);

        } finally {
            krypteringFutureList.stream().filter(f -> !f.isDone() && !f.isCancelled()).forEach(future -> future.cancel(true));
        }
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

    private void lastOppFiler(String soknadJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenummer, String navEkseternRefId, String token) {

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
            log.info(String.format("Sendte inn søknad og fikk digisosid: %s", EntityUtils.toString(response.getEntity())));
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

}
