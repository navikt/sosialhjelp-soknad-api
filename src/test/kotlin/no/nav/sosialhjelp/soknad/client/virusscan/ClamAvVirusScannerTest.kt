package no.nav.sosialhjelp.soknad.client.virusscan

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import no.nav.sosialhjelp.soknad.client.virusscan.dto.Result
import no.nav.sosialhjelp.soknad.client.virusscan.dto.ScanResult
import no.nav.sosialhjelp.soknad.consumer.redis.RedisUtils
import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class ClamAvVirusScannerTest {

    private val mockWebServer = MockWebServer()
    private val webClient = WebClient.create(mockWebServer.url("/").toString())
    private val enabled = true

    private val virusScanner = ClamAvVirusScanner(webClient, enabled)

    private val filnavn = "virustest"
    private val behandlingsId = "1100001"
    private val data = byteArrayOf()

    @BeforeEach
    fun setUp() {
        mockWebServer.start()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.close()
    }

    @Test
    fun scanFile_scanningIsEnabled_throwsException() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(RedisUtils.objectMapper.writeValueAsString(arrayOf(ScanResult(filnavn, Result.FOUND))))
        )
        assertThatExceptionOfType(OpplastingException::class.java)
            .isThrownBy { virusScanner.scan(filnavn, data, behandlingsId, "pdf") }
            .withMessageStartingWith("Fant virus")
    }

    @Test
    fun scanFile_scanningIsNotEnabled_doesNotThrowException() {
        val virusScanner2 = ClamAvVirusScanner(webClient, false)
        assertThatCode { virusScanner2.scan(filnavn, data, behandlingsId, "pdf") }
            .doesNotThrowAnyException()
    }

    @Test
    fun scanFile_filenameIsVirustest_isInfected() {
        System.setProperty("environment.name", "test")
        assertThatExceptionOfType(OpplastingException::class.java)
            .isThrownBy { virusScanner.scan("virustest", data, behandlingsId, "pdf") }
    }

    @Test
    fun scanFile_resultatHasWrongLength_isNotInfected() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(
                    RedisUtils.objectMapper.writeValueAsString(
                        arrayOf(
                            ScanResult("test", Result.FOUND),
                            ScanResult("test", Result.FOUND)
                        )
                    )
                )
        )
        assertThatCode { virusScanner.scan(filnavn, data, behandlingsId, "png") }
            .doesNotThrowAnyException()
    }

    @Test
    fun scanFile_resultatIsOK_isNotInfected() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(RedisUtils.objectMapper.writeValueAsString(arrayOf(ScanResult("test", Result.OK))))
        )
        assertThatCode { virusScanner.scan(filnavn, data, behandlingsId, "jpg") }
            .doesNotThrowAnyException()
    }

    @Test
    fun scanFile_resultatIsNotOK_isInfected() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(RedisUtils.objectMapper.writeValueAsString(arrayOf(ScanResult("test", Result.FOUND))))
        )
        assertThatExceptionOfType(OpplastingException::class.java)
            .isThrownBy { virusScanner.scan(filnavn, data, behandlingsId, "pdf") }
    }
}
