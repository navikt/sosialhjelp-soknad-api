package no.nav.sosialhjelp.soknad.kodeverk

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Kommuner
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Landkoder
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Postnummer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KodeverkDataServiceTest : KodeverkTestClass() {
    val kodeverkClient = mockk<KodeverkClient>()
    val kodeverkDataService = KodeverkDataService(kodeverkClient)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun hentKommuner() {
        every { kodeverkClient.hentKodeverk(Kommuner) } returns mockTermDto(TEST_KOMMUNER)
        val kommuner = kodeverkDataService.hentKodeverk(Kommuner)
        assertEquals(TEST_KOMMUNER, kommuner)
    }

    @Test
    fun hentPostnummer() {
        every { kodeverkClient.hentKodeverk(Postnummer) } returns mockTermDto(TEST_POSTNUMMER)
        val postnummer = kodeverkDataService.hentKodeverk(Postnummer)
        assertEquals(TEST_POSTNUMMER, postnummer)
    }

    @Test
    fun hentLandkoder() {
        every { kodeverkClient.hentKodeverk(Landkoder) } returns mockTermDto(TEST_LANDKODER)
        val landkoder = kodeverkDataService.hentKodeverk(Landkoder)
        assertEquals(TEST_LANDKODER, landkoder)
    }
}
