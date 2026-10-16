package no.nav.sosialhjelp.soknad.inntekt.husbanken

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import no.nav.sosialhjelp.soknad.v2.register.UserContextElement
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
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
        runBlocking(UserContextElement("dummy-token", "dummy-id")) {
            val fra = LocalDate.now().minusDays(30)
            val til = LocalDate.now()
            val inputStream = ClassLoader.getSystemResourceAsStream("husbanken/husbankenSvar.json")

            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(IOUtils.toString(inputStream, StandardCharsets.UTF_8)),
            )

            val bostotte = (husbankenClient.getBostotte(fra, til) as HusbankenResponse.Success).bostotte

            assertThat(bostotte).isNotNull
            assertThat(bostotte).isInstanceOf(BostotteDto::class.java)

            assertThat(bostotte.saker).hasSize(3)
            assertThat(bostotte.saker?.get(0)?.vedtak?.type).isEqualTo("INNVILGET")
            assertThat(bostotte.utbetalinger).hasSize(2)
            assertThat(bostotte.utbetalinger?.get(0)?.utbetalingsdato).isEqualTo(LocalDate.of(2019, 7, 20))
            assertThat(bostotte.utbetalinger?.get(0)?.belop?.toDouble()).isEqualTo(4300.5)
            assertThat(bostotte.utbetalinger?.get(1)?.utbetalingsdato).isEqualTo(LocalDate.of(2019, 8, 20))
            assertThat(bostotte.utbetalinger?.get(1)?.belop?.toDouble()).isEqualTo(4300.0)
            assertThat(mockWebServer.takeRequest().getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer dummy-token")
        }
    }

    @Test
    internal fun `hentBostotte 5xx feil`() {
        runBlocking(UserContextElement("dummy-token", "dummy-id")) {
            val fra = LocalDate.now().minusDays(30)
            val til = LocalDate.now()

            mockWebServer.enqueue(MockResponse().setResponseCode(503))

            val response = husbankenClient.getBostotte(fra, til) as HusbankenResponse.Error

            assertThat(response.e.statusCode.is5xxServerError).isTrue()
            assertThat(response.e.statusText).isEqualTo("Service Unavailable")
        }
    }

    @Test
    internal fun `hentBostotte 4xx feil`() {
        runBlocking(UserContextElement("dummy-token", "dummy-id")) {
            val fra = LocalDate.now().minusDays(30)
            val til = LocalDate.now()

            mockWebServer.enqueue(MockResponse().setResponseCode(400))
            val response = husbankenClient.getBostotte(fra, til) as HusbankenResponse.Error

            assertThat(response.e.statusCode.is4xxClientError).isTrue()
            assertThat(response.e.statusText).isEqualTo("Bad Request")
        }
    }

    @Test
    internal fun `Request returnerer null`() {
        runBlocking(UserContextElement("dummy-token", "dummy-id")) {
            val fra = LocalDate.now().minusDays(30)
            val til = LocalDate.now()

            mockWebServer.enqueue(MockResponse().setResponseCode(204))

            assertThat(husbankenClient.getBostotte(fra, til)).isEqualTo(HusbankenResponse.Null)
        }
    }
}
