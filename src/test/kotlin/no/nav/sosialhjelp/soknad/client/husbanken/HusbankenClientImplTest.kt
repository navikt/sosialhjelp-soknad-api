package no.nav.sosialhjelp.soknad.client.husbanken

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import no.nav.sosialhjelp.soknad.client.husbanken.dto.BostotteDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.nio.charset.StandardCharsets
import java.time.LocalDate


internal class HusbankenClientImplTest {

    private val mockWebServer = MockWebServer()
    private val webClient = WebClient.create(mockWebServer.url("/").toString())

    val husbankenClient = HusbankenClientImpl(webClient)

    @BeforeEach
    internal fun setUp() {
        mockWebServer.start()
    }

    @AfterEach
    internal fun tearDown() {
        mockWebServer.close()
    }

    @Test
    internal fun hentBostotte_returnererOptionalBostotte() {
        val fra = LocalDate.now().minusDays(30)
        val til = LocalDate.now()
        val inputStream = ClassLoader.getSystemResourceAsStream("husbanken/husbankenSvar.json")

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(IOUtils.toString(inputStream, StandardCharsets.UTF_8))
        )

        val bostotte = husbankenClient.hentBostotte("token", fra, til)

        assertThat(bostotte).isPresent
        assertThat(bostotte.get()).isInstanceOf(BostotteDto::class.java)

        val dto = bostotte.get()
        assertThat(dto.saker).hasSize(3)
        assertThat(dto.saker?.get(0)?.vedtak?.type).isEqualTo("INNVILGET")
        assertThat(dto.utbetalinger).hasSize(2)
        assertThat(dto.utbetalinger?.get(0)?.utbetalingsdato).isEqualTo(LocalDate.of(2019,7,20))
        assertThat(dto.utbetalinger?.get(0)?.belop?.toDouble()).isEqualTo(4300.5)
        assertThat(dto.utbetalinger?.get(1)?.utbetalingsdato).isEqualTo(LocalDate.of(2019,8,20))
        assertThat(dto.utbetalinger?.get(1)?.belop?.toDouble()).isEqualTo(4300.0)
    }

    @Test
    internal fun `hentBostotte 5xx feil`() {
        val fra = LocalDate.now().minusDays(30)
        val til = LocalDate.now()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(503)
        )

        val bostotte = husbankenClient.hentBostotte("token", fra, til)

        assertThat(bostotte).isNotPresent
    }

    @Test
    internal fun `hentBostotte 4xx feil`() {
        val fra = LocalDate.now().minusDays(30)
        val til = LocalDate.now()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
        )

        val bostotte = husbankenClient.hentBostotte("token", fra, til)

        assertThat(bostotte).isNotPresent
    }

    @Test
    internal fun `ping kaster ikke feil`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("OK")
        )

        assertThatNoException().isThrownBy { husbankenClient.ping() }
    }
}
