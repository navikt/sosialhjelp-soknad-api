package no.nav.sosialhjelp.soknad.consumer.fiks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.ks.fiks.streaming.klient.FilForOpplasting;
import no.ks.kryptering.CMSKrypteringImpl;
import no.ks.kryptering.CMSStreamKryptering;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilMetadata;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilOpplasting;
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DokumentlagerClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils.isMockAltProfil;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_INTEGRASJON_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_INTEGRASJON_PASSORD;
import static no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.isNonProduction;
import static no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.stripVekkFnutter;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;
import static org.slf4j.LoggerFactory.getLogger;

public class DigisosApiImpl implements DigisosApi {

    private static final Logger log = getLogger(DigisosApiImpl.class);
    private static final int SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000; // 5 minutter

    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper
            .createObjectMapper()
            .registerModule(new KotlinModule());

    private final KommuneInfoService kommuneInfoService;
    private final DigisosApiProperties properties;
    private final DokumentlagerClient dokumentlagerClient;
    private final HttpRequestRetryHandler retryHandler;
    private final ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy;

    private ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(Executors.newCachedThreadPool());
    private byte[] fiksPublicKey = null;
    private CMSStreamKryptering kryptering = new CMSKrypteringImpl();

    public DigisosApiImpl(
            DigisosApiProperties properties,
            KommuneInfoService kommuneInfoService,
            DokumentlagerClient dokumentlagerClient
    ) {
        this.kommuneInfoService = kommuneInfoService;
        this.properties = properties;
        this.dokumentlagerClient = dokumentlagerClient;
        this.retryHandler = new DefaultHttpRequestRetryHandler();
        this.serviceUnavailableRetryStrategy = new FiksServiceUnavailableRetryStrategy();
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

    @Override
    public void ping() {
        Map<String, KommuneInfo> kommuneInfo = kommuneInfoService.hentAlleKommuneInfo();
        if (kommuneInfo.isEmpty()) {
            throw new IllegalStateException("Fikk ikke kontakt med digisosapi");
        }
    }

    private HttpClientBuilder clientBuilder() {
        return HttpClientBuilder.create()
                .setRetryHandler(retryHandler)
                .setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy)
                .useSystemProperties();
    }

    @Override
    public String krypterOgLastOppFiler(String soknadJson, String tilleggsinformasjonJson, String vedleggJson, List<FilOpplasting> dokumenter, String kommunenr, String behandlingsId, String token) {
        List<Future<Void>> krypteringFutureList = Collections.synchronizedList(new ArrayList<>(dokumenter.size()));
        String digisosId;
        try {
            X509Certificate fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate(token);
            digisosId = lastOppFiler(soknadJson, tilleggsinformasjonJson, vedleggJson, dokumenter.stream()
                    .map(dokument -> new FilOpplasting(dokument.metadata, krypter(dokument.data, krypteringFutureList, fiksX509Certificate)))
                    .collect(Collectors.toList()), kommunenr, behandlingsId, token);

            waitForFutures(krypteringFutureList);

        } finally {
            krypteringFutureList.stream().filter(f -> !f.isDone() && !f.isCancelled()).forEach(future -> future.cancel(true));
        }
        return digisosId;
    }

    private InputStream krypter(InputStream dokumentStream, List<Future<Void>> krypteringFutureList, X509Certificate fiksX509Certificate) {
        PipedInputStream pipedInputStream = new PipedInputStream();
        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            Future<Void> krypteringFuture =
                    executor.submit(() -> {
                        try {
                            if(isNonProduction() && isMockAltProfil()) {
                                IOUtils.copy(dokumentStream, pipedOutputStream);
                            } else {
                                kryptering.krypterData(pipedOutputStream, dokumentStream, fiksX509Certificate, Security.getProvider("BC"));
                            }
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

        try (CloseableHttpClient client = clientBuilder().setDefaultRequestConfig(requestConfig).build()) {
            HttpPost post = new HttpPost(properties.getDigisosApiEndpoint() + getLastOppFilerPath(kommunenummer, behandlingsId));

            post.setHeader("requestid", UUID.randomUUID().toString());
            post.setHeader(AUTHORIZATION.name(), token);
            post.setHeader(HEADER_INTEGRASJON_ID, properties.getIntegrasjonsidFiks());
            post.setHeader(HEADER_INTEGRASJON_PASSORD, properties.getIntegrasjonpassordFiks());

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
            log.info("Sendte inn søknad {} til kommune {} og fikk digisosid: {}", behandlingsId, kommunenummer, digisosId);
            return digisosId;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Opplasting av %s til fiks-digisos-api feilet", behandlingsId), e);
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
