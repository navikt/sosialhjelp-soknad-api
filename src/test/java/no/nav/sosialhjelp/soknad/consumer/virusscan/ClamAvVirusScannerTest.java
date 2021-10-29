package no.nav.sosialhjelp.soknad.consumer.virusscan;

import com.fasterxml.jackson.core.JsonProcessingException;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;

import static no.nav.sosialhjelp.soknad.consumer.redis.RedisUtils.objectMapper;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ClamAvVirusScannerTest {

    private final MockWebServer mockWebServer = new MockWebServer();
    private final WebClient webClient = WebClient.create(mockWebServer.url("/").toString());
    private final URI uri = URI.create("www.test.com");

    private final ClamAvVirusScanner virusScanner = new ClamAvVirusScanner(uri, webClient);

    private String filnavn = "virustest";
    private String behandlingsId = "1100001";
    private byte[] data = new byte[]{};

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer.start();
        setField(virusScanner, "enabled", true);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.close();
        System.clearProperty("environment.name");
    }

    @Test
    void scanFile_scanningIsEnabled_throwsException() throws JsonProcessingException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(new ScanResult[]{new ScanResult(filnavn, Result.FOUND)}))
        );

        assertThatExceptionOfType(OpplastingException.class)
                .isThrownBy(() -> virusScanner.scan(filnavn, data, behandlingsId, "pdf"))
                .withMessageStartingWith("Fant virus");
    }

    @Test
    void scanFile_scanningIsNotEnabled_doesNotThrowException() {
        setField(virusScanner, "enabled", false);

        assertThatCode(() -> virusScanner.scan(filnavn, data, behandlingsId, "pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    void scanFile_filenameIsVirustest_isInfected() {
        System.setProperty("environment.name", "test");

        assertThatExceptionOfType(OpplastingException.class)
                .isThrownBy(() -> virusScanner.scan("virustest", data, behandlingsId, "pdf"));
    }

    @Test
    void scanFile_resultatHasWrongLength_isNotInfected() throws JsonProcessingException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(new ScanResult[]{
                                new ScanResult("test", Result.FOUND),
                                new ScanResult("test", Result.FOUND)
                        }))
        );

        assertThatCode(() -> virusScanner.scan(filnavn, data, behandlingsId, "png"))
                .doesNotThrowAnyException();
    }

    @Test
    void scanFile_resultatIsOK_isNotInfected() throws JsonProcessingException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(new ScanResult[]{new ScanResult("test", Result.OK)}))
        );

        assertThatCode(() -> virusScanner.scan(filnavn, data, behandlingsId, "jpg"))
                .doesNotThrowAnyException();
    }

    @Test
    void scanFile_resultatIsNotOK_isInfected() throws JsonProcessingException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(new ScanResult[]{new ScanResult("test", Result.FOUND)}))
        );

        assertThatExceptionOfType(OpplastingException.class)
                .isThrownBy(() -> virusScanner.scan(filnavn, data, behandlingsId, "pdf"));
    }
}
