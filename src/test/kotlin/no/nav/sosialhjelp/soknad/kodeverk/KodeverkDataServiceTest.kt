package no.nav.sosialhjelp.soknad.kodeverk

import io.mockk.clearAllMocks
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Kommuner
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Landkoder
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Postnummer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.nio.charset.StandardCharsets

class KodeverkDataServiceTest : KodeverkTestClass() {
    private val mockWebServer = MockWebServer()
    val kodeverkClient = KodeverkClient(mockWebServer.url("/").toString(), WebClient.builder())
    val kodeverkDataService = KodeverkDataService(kodeverkClient)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    private fun prepareMockWebServerResponse(kodeverkNavn: String) = mockWebServer.enqueue(
        MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(IOUtils.toString(ClassLoader.getSystemResourceAsStream("kodeverk/$kodeverkNavn.json"), StandardCharsets.UTF_8))
    )

    @Test
    fun hentKommuner() {
        prepareMockWebServerResponse(Kommuner)

        val kommuner = kodeverkDataService.hentKodeverk(Kommuner)

        assertEquals(kommuner[KOMMUNE_OSLO_NUMMER], KOMMUNE_OSLO_TERM)
        assertEquals(kommuner[KOMMUNE_BERGEN_NUMMER], KOMMUNE_BERGEN_TERM)
    }

    @Test
    fun hentPostnummer() {
        prepareMockWebServerResponse(Postnummer)

        val postnummer = kodeverkDataService.hentKodeverk(Postnummer)

        assertEquals(postnummer[POSTNUMMER_RØDTVET_KODE], POSTNUMMER_RØDTVET_TERM)
        assertEquals(postnummer[POSTNUMMER_SAMFUNDET_KODE], POSTNUMMER_SAMFUNDET_TERM)
        assertEquals(postnummer[POSTNUMMER_SESAM_STASJON_KODE], POSTNUMMER_SESAM_STASJON_TERM)
    }

    @Test
    fun hentLandkoder() {
        prepareMockWebServerResponse(Landkoder)

        val landkoder = kodeverkDataService.hentKodeverk(Landkoder)

        assertEquals(landkoder[LAND_NORGE_KODE], LAND_NORGE_TERM)
        assertEquals(landkoder[LAND_SVERIGE_KODE], LAND_SVERIGE_TERM)
    }
}
