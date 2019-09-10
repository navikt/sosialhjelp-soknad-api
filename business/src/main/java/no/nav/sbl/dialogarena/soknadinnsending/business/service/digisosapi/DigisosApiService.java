package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.fiks.streaming.klient.*;
import no.ks.fiks.streaming.klient.authentication.PersonAuthenticationStrategy;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class DigisosApiService {
    @Inject
    private IdPortenService idPortenService;

    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();

    public List<KommuneInfo> hentKommuneInfo() {
        IdPortenService.AcceessToken accessToken = idPortenService.getAccessToken();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet http = new HttpGet("https://api.fiks.test.ks.no/digisos/api/v1/nav/kommune");
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", "fiksIntegrasjonId");
            http.setHeader("IntegrasjonPassord", "fiksIntegrasjon");
            http.setHeader("Authorization", "Bearer " + accessToken.token);

            CloseableHttpResponse response = client.execute(http);
            return Arrays.asList(objectMapper.readValue(EntityUtils.toString(response.getEntity()), KommuneInfo[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KommuneInfo hentKommuneInfo(String kommunenr) {
        IdPortenService.AcceessToken accessToken = idPortenService.getAccessToken();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet http = new HttpGet("https://api.fiks.test.ks.no/digisos/api/v1/nav/kommune/" + kommunenr);
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", "fiksIntegrasjonId");
            http.setHeader("IntegrasjonPassord", "fiksIntegrasjon");
            http.setHeader("Authorization", "Bearer " + accessToken.token);

            CloseableHttpResponse response = client.execute(http);
            return objectMapper.readValue(EntityUtils.toString(response.getEntity()), KommuneInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendSoknad(String kommunenr, String navEkseternRefId) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet http = new HttpGet(String.format("https://api.fiks.test.ks.no/digisos/api/v1/soknader/%s/%s/", kommunenr, navEkseternRefId) );
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", "fiksIntegrasjonId");
            http.setHeader("IntegrasjonPassord", "fiksIntegrasjon");
            http.setHeader("Transfer-Encoding", "chunked");
           // http.setHeader("Authorization", "Bearer " + accessToken.token);

            CloseableHttpResponse response = client.execute(http);
           // return objectMapper.readValue(EntityUtils.toString(response.getEntity()), KommuneInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class KommuneInfo {
        public final String kommunenummer;
        public final Boolean kanMottaSoknader;
        public final Boolean kanOppdatereStatus;

        public KommuneInfo(String kommunenummer, Boolean kanMottaSoknader, Boolean kanOppdatereStatus) {
            this.kommunenummer = kommunenummer;
            this.kanMottaSoknader = kanMottaSoknader;
            this.kanOppdatereStatus = kanOppdatereStatus;
        }
    }

    public X509Certificate getDokumentlagerPublicKeyX509Certificate() {
        StreamingKlient streamingKlient = new StreamingKlient(new PersonAuthenticationStrategy("token"));
        List<HttpHeader> httpHeaders = Collections.singletonList(getHttpHeaderRequestId());
        KlientResponse<byte[]> response = streamingKlient.sendGetRawContentRequest(HttpMethod.GET, "https://api.fiks.test.ks.no/", "/digisos/api/v1/dokumentlager-public-key", httpHeaders);

        byte[] publicKey = response.getResult();
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(publicKey));

        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

    }

    private HttpHeader getHttpHeaderRequestId() {
        String requestId = UUID.randomUUID().toString();
        if (MDC.get("requestid") != null) {
            requestId = MDC.get("requestid");
        }
        return HttpHeader.builder().headerName("requestid").headerValue(requestId).build();
    }

    public KlientResponse<List<FiksData.DokumentInfo>> krypterOgLastOppFiler(List<FilOpplasting> dokumenter, UUID fiksOrgId, UUID digisosId) {

        final List<CompletableFuture<Void>> krypteringFutureList = Collections.synchronizedList(new ArrayList<>(dokumenter.size()));
        try {
            KlientResponse<List<FiksData.DokumentInfo>> opplastetFiler = lastOppFiler(dokumenter.stream()
                    .map(dokument -> new FilOpplasting(dokument.getMetadata(), krypter(dokument.getData(), krypteringFutureList)))
                    .collect(Collectors.toList()), fiksOrgId, digisosId);


            waitForFutures(krypteringFutureList);
            log.info("{} dokumenter lagt til digisosId {} på fiksOrg {}", dokumenter.size(), digisosId, fiksOrgId);
            return opplastetFiler;
        } finally {
            krypteringFutureList.stream().filter(f -> !f.isDone() && !f.isCancelled()).forEach(future -> future.cancel(true));
        }
    }

    private void waitForFutures(List<CompletableFuture<Void>> krypteringFutureList) {
        final CompletableFuture<Void> allFutures = CompletableFuture.allOf(krypteringFutureList.toArray(new CompletableFuture[]{}));
        try {
            allFutures.get(300, TimeUnit.SECONDS);
        } catch (CompletionException e) {
            throw new IllegalStateException(e.getCause());
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public class FilOpplasting {

        public FilMetadata metadata;
        public InputStream data;

        FilOpplasting metadata(FilMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        FilOpplasting data(InputStream data) {
            this.data = data;
            return this;
        }

    }
    public class FilMetadata {

         public String filnavn;
         public String mimetype;
         public Long storrelse;

        public FilMetadata withFilnavn(String filnavn) {
            this.filnavn = filnavn;
            return this;
        }

        public FilMetadata withMimetype(String mimetype) {
            this.mimetype = mimetype;
            return this;
        }

        public FilMetadata withStorrelse(Long storrelse) {
            this.storrelse = storrelse;
            return this;
        }
    }

    public class DokumentInfo {

        @JsonCreator
        public DokumentInfo(@JsonProperty("filnavn")  String filnavn, @JsonProperty("dokumentlagerDokumentId")  UUID dokumentlagerDokumentId, @JsonProperty("storrelse")  Long storrelse) {
            this.filnavn = filnavn;
            this.dokumentlagerDokumentId = dokumentlagerDokumentId;
            this.storrelse = storrelse;
        }


        public String filnavn;


        public UUID dokumentlagerDokumentId;


        public Long storrelse;

    }

    public KlientResponse<List<DokumentInfo>> lastOppFiler( List<FilOpplasting> dokumenter,  UUID fiksOrgId,  UUID digisosId) {

        MultipartContentProviderBuilder multipartBuilder = new MultipartContentProviderBuilder();

        List<FilForOpplasting<Object>> filer = new ArrayList<>();

        dokumenter.forEach(dokument -> filer.add(FilForOpplasting.builder()
                .filnavn(dokument.metadata.filnavn)
                .metadata(new FilMetadata()
                        .filnavn(dokument.metadata.filnavn)
                        .mimetype(dokument.metadata.mimetype)
                        .storrelse(dokument.metadata.storrelse)
                        )
                .data(dokument.data)
                .build()));

        multipartBuilder.addFileData(filer);
        MultiPartContentProvider multiPartContentProvider = multipartBuilder.build();

        List<HttpHeader> httpHeaders = Collections.singletonList(getHttpHeaderRequestId());

        KlientResponse<List<DokumentInfo>> response = streamingKlient.sendRequest(multiPartContentProvider, HttpMethod.POST, baseUrl, getLastOppFilerPath(fiksOrgId, digisosId), httpHeaders, new TypeReference<List<DokumentInfo>>() {});

        return response;

    }
}
