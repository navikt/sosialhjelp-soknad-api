package no.nav.sosialhjelp.soknad.vedlegg.virusscan

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.dto.Result
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.dto.ScanResult
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class VirusScannerTest {

    private val mockWebServer = MockWebServer()
    private val webClient = WebClient.create(mockWebServer.url("/").toString())
    private val enabled = true

    private val virusScanner = VirusScanner(webClient, enabled)

    private val filnavn = "virustest"
    private val behandlingsId = "1100001"
    private val data = byteArrayOf()

    @BeforeEach
    fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns false
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.close()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun scanFile_scanningIsEnabled_throwsException() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(redisObjectMapper.writeValueAsString(arrayOf(ScanResult(filnavn, Result.FOUND))))
        )
        assertThatExceptionOfType(OpplastingException::class.java)
            .isThrownBy { virusScanner.scan(filnavn, data, behandlingsId, "pdf") }
            .withMessageStartingWith("Fant virus")
    }

    @Test
    fun scanFile_scanningIsNotEnabled_doesNotThrowException() {
        val virusScanner2 = VirusScanner(webClient, false)
        assertThatCode { virusScanner2.scan(filnavn, data, behandlingsId, "pdf") }
            .doesNotThrowAnyException()
    }

    @Test
    fun scanFile_filenameIsVirustest_isInfected() {
        every { MiljoUtils.isNonProduction() } returns true
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
                    redisObjectMapper.writeValueAsString(
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
                .setBody(redisObjectMapper.writeValueAsString(arrayOf(ScanResult("test", Result.OK))))
        )
        assertThatCode { virusScanner.scan(filnavn, data, behandlingsId, "jpg") }
            .doesNotThrowAnyException()
    }

    @Test
    fun scanFile_resultatIsFound_isInfected() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(redisObjectMapper.writeValueAsString(arrayOf(ScanResult("test", Result.FOUND))))
        )
        assertThatExceptionOfType(OpplastingException::class.java)
            .isThrownBy { virusScanner.scan(filnavn, data, behandlingsId, "pdf") }
    }

    @Test
    fun scanFile_resultatIsError_isInfected() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(redisObjectMapper.writeValueAsString(arrayOf(ScanResult("test", Result.ERROR))))
        )
        assertThatExceptionOfType(OpplastingException::class.java)
            .isThrownBy { virusScanner.scan(filnavn, data, behandlingsId, "pdf") }
    }
}
