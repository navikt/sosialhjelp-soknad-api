package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.nio.charset.StandardCharsets
import java.time.LocalDate

internal class HusbankenClientTest {

    private val mockWebServer = MockWebServer()
    private val webClient = WebClient.create(mockWebServer.url("/").toString())

    private val husbankenClient = HusbankenClient(webClient)

    @AfterEach
    internal fun tearDown() {
        mockWebServer.close()
    }

    @Test
    internal fun hentBostotte_returnererBostotte() {
        val fra = LocalDate.now().minusDays(30)
        val til = LocalDate.now()
        val inputStream = ClassLoader.getSystemResourceAsStream("husbanken/husbankenSvar.json")

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(IOUtils.toString(inputStream, StandardCharsets.UTF_8)),
        )

        val bostotte = husbankenClient.hentBostotte("token", fra, til)

        assertThat(bostotte).isNotNull
        assertThat(bostotte).isInstanceOf(BostotteDto::class.java)

        assertThat(bostotte?.saker).hasSize(3)
        assertThat(bostotte?.saker?.get(0)?.vedtak?.type).isEqualTo("INNVILGET")
        assertThat(bostotte?.utbetalinger).hasSize(2)
        assertThat(bostotte?.utbetalinger?.get(0)?.utbetalingsdato).isEqualTo(LocalDate.of(2019, 7, 20))
        assertThat(bostotte?.utbetalinger?.get(0)?.belop?.toDouble()).isEqualTo(4300.5)
        assertThat(bostotte?.utbetalinger?.get(1)?.utbetalingsdato).isEqualTo(LocalDate.of(2019, 8, 20))
        assertThat(bostotte?.utbetalinger?.get(1)?.belop?.toDouble()).isEqualTo(4300.0)
    }

    @Test
    internal fun `hentBostotte 5xx feil`() {
        val fra = LocalDate.now().minusDays(30)
        val til = LocalDate.now()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(503),
        )

        val bostotte = husbankenClient.hentBostotte("token", fra, til)

        assertThat(bostotte).isNull()
    }

    @Test
    internal fun `hentBostotte 4xx feil`() {
        val fra = LocalDate.now().minusDays(30)
        val til = LocalDate.now()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400),
        )

        val bostotte = husbankenClient.hentBostotte("token", fra, til)

        assertThat(bostotte).isNull()
    }

    @Test
    internal fun `ping kaster ikke feil`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("OK"),
        )

        assertThatNoException().isThrownBy { husbankenClient.ping() }
    }
}
