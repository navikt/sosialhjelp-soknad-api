package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.fiks.streaming.klient.FilForOpplasting;
import no.ks.kryptering.CMSKrypteringImpl;
import no.ks.kryptering.CMSStreamKryptering;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.DokumentInfo;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.FilMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.FilOpplasting;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class KrypteringService {
    private static final Logger log = getLogger(KrypteringService.class);

    private ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(Executors.newCachedThreadPool());

    public List<DokumentInfo> krypterOgLastOppFiler(List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token) {
        log.info(String.format("Starter kryptering av filer, skal sende til %s %s %s", kommunenr, navEkseternRefId, token));
        List<Future<Void>> krypteringFutureList = Collections.synchronizedList(new ArrayList<>(dokumenter.size()));
        try {
            List<DokumentInfo> opplastetFiler = lastOppFiler(dokumenter.stream()
                    .map(dokument -> new FilOpplasting(dokument.metadata, krypter(dokument.data, krypteringFutureList, token)))
                    .collect(Collectors.toList()), kommunenr, navEkseternRefId, token);

            log.info("Venter på at filene blir ferdig kryptert");
            waitForFutures(krypteringFutureList);
            log.info("Filene ferdig kryptert");
            return opplastetFiler;
        } finally {
            krypteringFutureList.stream().filter(f -> !f.isDone() && !f.isCancelled()).forEach(future -> future.cancel(true));
        }
    }

    private X509Certificate getDokumentlagerPublicKeyX509Certificate(String token) {
        byte[] publicKey = new byte[0];
        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build();) {
            log.info("Henter certifikat");
            HttpUriRequest request = RequestBuilder.get().setUri(System.getProperty("digisos_api_baseurl") + "/digisos/api/v1/dokumentlager-public-key")
                    .addHeader("Accept", MediaType.WILDCARD)
                    .addHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"))
                    .addHeader("IntegrasjonPassord", System.getProperty("integrasjonpassord_fiks"))
                    .addHeader("Authorization", "Bearer " + token).build();

            CloseableHttpResponse response = client.execute(request);
            publicKey = IOUtils.toByteArray(response.getEntity().getContent());
            log.info("Hentet sertifikat");
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

    private InputStream krypter(InputStream dokumentStream, List<Future<Void>> krypteringFutureList, String token) {
        CMSStreamKryptering kryptering = new CMSKrypteringImpl();
        X509Certificate certificate = getDokumentlagerPublicKeyX509Certificate(token);

        PipedInputStream pipedInputStream = new PipedInputStream();
        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            log.info("Starting encryption submit...");
            Future<Void> krypteringFuture =
                    executor.submit(() -> {
                        try {
                            log.info("Starting encryption...");
                            kryptering.krypterData(pipedOutputStream, dokumentStream, certificate, Security.getProvider("BC"));
                            log.info("Encryption completed");
                        } catch (Exception e) {
                            log.error("Encryption failed, setting exception on encrypted InputStream", e);
                            throw new IllegalStateException("An error occurred during encryption", e);
                        } finally {
                            try {
                                log.info("Closing encryption OutputStream");
                                pipedOutputStream.close();
                                log.info("Encryption OutputStream closed");
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

    private List<DokumentInfo> lastOppFiler(List<FilOpplasting> dokumenter, String kommunenummer, String navEkseternRefId, String token) {


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
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (FilForOpplasting<Object> objectFilForOpplasting : filer) {
            entitybuilder.addBinaryBody(objectFilForOpplasting.getFilnavn(), objectFilForOpplasting.getData());
        }

        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build();) {
            HttpPost post = new HttpPost(System.getProperty("digisos_api_baseurl") + getLastOppFilerPath(kommunenummer, navEkseternRefId));

            post.setHeader("Accept", MediaType.MEDIA_TYPE_WILDCARD);
            post.setHeader("requestid", UUID.randomUUID().toString());
            post.setHeader("Authorization", "Bearer " + token);
            post.setEntity(entitybuilder.build());
            CloseableHttpResponse response = client.execute(post);
            String x = EntityUtils.toString(response.getEntity());
            return Arrays.asList(new ObjectMapper().readValue(x, DokumentInfo[].class));
        } catch (IOException e) {
            log.error("", e);
        }

        return null;

    }

    private String getLastOppFilerPath(String kommunenummer, String navEkseternRefId) {
        return String.format("/digisos/api/v1/soknader%s/%s", kommunenummer, navEkseternRefId);
    }
}
