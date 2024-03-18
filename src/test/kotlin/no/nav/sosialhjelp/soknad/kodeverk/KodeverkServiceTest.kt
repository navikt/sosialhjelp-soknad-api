package no.nav.sosialhjelp.soknad.kodeverk

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Kommuner
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Landkoder
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Postnummer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KodeverkServiceTest : KodeverkTestClass() {
    private val kodeverkDataService: KodeverkDataService = mockk()
    private val kodeverkService = KodeverkService(kodeverkDataService)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    internal fun gjettKommunenummer() {
        every { kodeverkDataService.hentKodeverk(Kommuner) } returns TEST_KOMMUNER
        for ((kode, term) in TEST_KOMMUNER.entries) assertThat(kodeverkService.gjettKommunenummer(term)).isEqualTo(kode)
        assertThat(kodeverkService.gjettKommunenummer("ukjentKommunenavn")).isNull()
    }

    @Test
    internal fun getKommunenavn() {
        every { kodeverkDataService.hentKodeverk(Kommuner) } returns TEST_KOMMUNER
        for ((kode, term) in TEST_KOMMUNER.entries) assertThat(kodeverkService.getKommunenavn(kode)).isEqualTo(term)
        assertThat(kodeverkService.getKommunenavn("ukjentKommunenummer")).isNull()
    }

    @Test
    internal fun getPoststed() {
        every { kodeverkDataService.hentKodeverk(Postnummer) } returns TEST_POSTNUMMER
        for ((kode, term) in TEST_POSTNUMMER.entries) assertThat(kodeverkService.getPoststed(kode)).isEqualTo(term)
        assertThat(kodeverkService.getPoststed("ukjentPostnummer")).isNull()
    }

    @Test
    internal fun getLand() {
        every { kodeverkDataService.hentKodeverk(Landkoder) } returns TEST_LANDKODER
        for ((kode, term) in TEST_LANDKODER.entries) assertThat(kodeverkService.getLand(kode)).isEqualTo(term)
        assertThat(kodeverkService.getLand("ukjentLandkode")).isNull()
    }
}
