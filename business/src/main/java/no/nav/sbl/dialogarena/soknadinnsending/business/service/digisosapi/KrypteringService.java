package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import com.fasterxml.jackson.core.type.TypeReference;
import no.ks.fiks.streaming.klient.*;
import no.ks.fiks.streaming.klient.authentication.PersonAuthenticationStrategy;
import no.ks.kryptering.CMSKrypteringImpl;
import no.ks.kryptering.CMSStreamKryptering;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.DokumentInfo;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.FilMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.FilOpplasting;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class KrypteringService {
    private static final Logger log = getLogger(KrypteringService.class);

    private ExecutorService executor = Executors.newFixedThreadPool(4);

    public KlientResponse<List<DokumentInfo>> krypterOgLastOppFiler(List<FilOpplasting> dokumenter, String kommunenr, String navEkseternRefId, String token) {

        List<CompletableFuture<Void>> krypteringFutureList = Collections.synchronizedList(new ArrayList<>(dokumenter.size()));
        try {
            KlientResponse<List<DokumentInfo>> opplastetFiler = lastOppFiler(dokumenter.stream()
                    .map(dokument -> new FilOpplasting(dokument.metadata, krypter(dokument.data, krypteringFutureList, token)))
                    .collect(Collectors.toList()), kommunenr, navEkseternRefId);


            waitForFutures(krypteringFutureList);
            return opplastetFiler;
        } finally {
            krypteringFutureList.stream().filter(f -> !f.isDone() && !f.isCancelled()).forEach(future -> future.cancel(true));
        }
    }

    private X509Certificate getDokumentlagerPublicKeyX509Certificate(String token) {
        StreamingKlient streamingKlient = new StreamingKlient(new PersonAuthenticationStrategy(token));
        List<HttpHeader> httpHeaders = Collections.singletonList(getHttpHeaderRequestId());
        KlientResponse<byte[]> response = streamingKlient.sendGetRawContentRequest(HttpMethod.GET, System.getProperty("digisos_api_baseurl") , "/digisos/api/v1/dokumentlager-public-key", httpHeaders);

        byte[] publicKey = response.getResult();
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(publicKey));

        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream krypter(InputStream dokumentStream, List<CompletableFuture<Void>> krypteringFutureList, String token) {
        CMSStreamKryptering kryptering = new CMSKrypteringImpl();
        X509Certificate certificate = getDokumentlagerPublicKeyX509Certificate(token);

        PipedInputStream pipedInputStream = new PipedInputStream();
        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            CompletableFuture<Void> krypteringFuture = CompletableFuture.runAsync(() -> {
                try {
                    log.debug("Starting encryption...");
                    kryptering.krypterData(pipedOutputStream, dokumentStream, certificate, Security.getProvider("BC"));
                    log.debug("Encryption completed");
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

            }, executor);
            krypteringFutureList.add(krypteringFuture);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pipedInputStream;
    }

    private HttpHeader getHttpHeaderRequestId() {
        String requestId = UUID.randomUUID().toString();
        if (MDC.get("requestid") != null) {
            requestId = MDC.get("requestid");
        }
        return HttpHeader.builder().headerName("requestid").headerValue(requestId).build();
    }

    private void waitForFutures(List<CompletableFuture<Void>> krypteringFutureList) {
         CompletableFuture<Void> allFutures = CompletableFuture.allOf(krypteringFutureList.toArray(new CompletableFuture[]{}));
        try {
            allFutures.get(300, TimeUnit.SECONDS);
        } catch (CompletionException e) {
            throw new IllegalStateException(e.getCause());
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private KlientResponse<List<DokumentInfo>> lastOppFiler(List<FilOpplasting> dokumenter, String kommunenummer, String navEkseternRefId) {

        MultipartContentProviderBuilder multipartBuilder = new MultipartContentProviderBuilder();

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

        multipartBuilder.addFileData(filer);
        MultiPartContentProvider multiPartContentProvider = multipartBuilder.build();

        List<HttpHeader> httpHeaders = Collections.singletonList(getHttpHeaderRequestId());
        StreamingKlient streamingKlient = new StreamingKlient(new PersonAuthenticationStrategy("token"));

        return streamingKlient.sendRequest(multiPartContentProvider, HttpMethod.POST, "https://api.fiks.test.ks.no/", getLastOppFilerPath(kommunenummer, navEkseternRefId), httpHeaders, new TypeReference<List<DokumentInfo>>() {});

    }

    private String getLastOppFilerPath(String kommunenummer, String navEkseternRefId) {
        return String.format("/digisos/api/v1/soknader%s/%s", kommunenummer, navEkseternRefId);
    }
}
