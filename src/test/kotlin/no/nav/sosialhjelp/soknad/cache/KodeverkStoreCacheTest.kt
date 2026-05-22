package no.nav.sosialhjelp.soknad.cache

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import io.mockk.verify
import no.nav.sosialhjelp.soknad.kodeverk.BeskrivelseDto
import no.nav.sosialhjelp.soknad.kodeverk.BetydningDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkCacheConfig
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkClient
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkStore
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

class KodeverkStoreCacheTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var kodeverkService: KodeverkService

    @Autowired
    private lateinit var kodeverkStore: KodeverkStore

    @Autowired
    private lateinit var cacheManager: CacheManager

    @MockkSpyBean
    private lateinit var kodeverkClient: KodeverkClient

    private val mapper = JsonMapper()

    @BeforeEach
    fun setup() {
        cacheManager.getCache(KodeverkCacheConfig.CACHE_NAME)!!.clear()
    }

    @Test
    fun `Hente kodeverk kommuner skal caches`() {
        every { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) } returns createKommuner()

        val kommunenummer = "0301"
        val kommunenavn = kodeverkService.getKommunenavn(kommunenummer)!!

        assertCacheExists(Kodeverksnavn.KOMMUNER, kommunenummer, kommunenavn)
        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) }

        every { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) } returns createKommuner(null)

        kodeverkService.getKommunenavn(kommunenummer).also { assertThat(it).isEqualTo(kommunenavn) }
        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) }
    }

    @Test
    fun `Hente kodeverk poststed skal caches`() {
        every { kodeverkClient.hentKodeverk(Kodeverksnavn.POSTNUMMER) } returns createPoststed()

        val postnummer = "2730"
        val poststed = kodeverkService.getPoststed(postnummer)!!

        assertCacheExists(Kodeverksnavn.POSTNUMMER, postnummer, poststed)
        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.POSTNUMMER) }

        every { kodeverkClient.hentKodeverk(Kodeverksnavn.POSTNUMMER) } returns createPoststed(null)

        kodeverkService.getPoststed(postnummer).also { assertThat(it).isEqualTo(poststed) }
        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.POSTNUMMER) }
    }

    @Test
    fun `Hente kodeverk land skal caches`() {
        val landkode = "nor"

        every { kodeverkClient.hentKodeverk(Kodeverksnavn.LANDKODER) } returns createLand()

        val land = kodeverkService.getLand(landkode)!!

        assertCacheExists(Kodeverksnavn.LANDKODER, landkode, land)

        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.LANDKODER) }

        every { kodeverkClient.hentKodeverk(Kodeverksnavn.LANDKODER) } returns createKommuner(null)

        kodeverkService.getLand(landkode)
            .also { assertThat(it).isEqualTo(land) }

        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.LANDKODER) }
    }

    @Test
    fun `Hent med no cache skal evicte cachen med riktig key`() {
        val kommunenummerOslo = "0301"
        val landkode = "nor"

        every { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) } returns createKommuner()
        every { kodeverkClient.hentKodeverk(Kodeverksnavn.LANDKODER) } returns createLand()

        val kommunenavnOslo = kodeverkStore.hentKodeverk(Kodeverksnavn.KOMMUNER)[kommunenummerOslo]!!
        val land = kodeverkStore.hentKodeverk(Kodeverksnavn.LANDKODER)["nor"]!!

        assertCacheExists(Kodeverksnavn.KOMMUNER, kommunenummerOslo, kommunenavnOslo)
        assertCacheExists(Kodeverksnavn.LANDKODER, landkode, land)

        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) }
        verify (exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.LANDKODER) }

        // tømmer cachen for nevnt key
        kodeverkStore.hentKodeverkNoCache(Kodeverksnavn.KOMMUNER)[kommunenummerOslo]
            .also { assertThat(it).isEqualTo(kommunenavnOslo) }

        assertCacheExists(Kodeverksnavn.LANDKODER, landkode, land)

        cacheManager.getCache(KodeverkCacheConfig.CACHE_NAME)!!
            .let { cache -> cache.get(Kodeverksnavn.KOMMUNER)?.get() }
            .also { assertThat(it).isNull() }

        verify (exactly = 2) { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) }
    }

    private fun assertCacheExists(cacheKey: Kodeverksnavn, kode: String, kodeValue: String) =
        cacheManager.getCache(KodeverkCacheConfig.CACHE_NAME)!!
            .let { cache -> cache.get(cacheKey)!!.get() }
            .let {  mapper.convertValue(it, object : TypeReference<Map<String, String?>>() {})  }
            .also { assertThat(it[kode]).isEqualTo(kodeValue) }
}

private fun createKommuner(kommunenummer: String? = "0301", navn: String? = "Oslo"): KodeverkDto {
    return if (kommunenummer == null || navn == null) KodeverkDto(betydninger = emptyMap())
    else
        KodeverkDto(
            betydninger = mapOf(
                kommunenummer to listOf(
                    BetydningDto(
                        gyldigFra = LocalDate.of(2024, 1, 1),
                        gyldigTil = LocalDate.now().plusYears(10),
                        beskrivelser = mapOf(
                            "nb" to BeskrivelseDto(
                                term = navn,
                                tekst = navn,
                            )
                        )
                    )
                )
            )
        )
}

private fun createPoststed(postnummer: String? = "2730"): KodeverkDto {
    return if (postnummer == null) KodeverkDto(betydninger = emptyMap())
    else
        KodeverkDto(
            betydninger = mapOf(
                postnummer to listOf(
                    BetydningDto(
                        gyldigFra = LocalDate.of(2024, 1, 1),
                        gyldigTil = LocalDate.now().plusYears(10),
                        beskrivelser = mapOf(
                            "nb" to BeskrivelseDto(
                                term = "Lunner",
                                tekst = "Lunner",
                            )
                        )
                    )
                )
            )
        )
}

private fun createLand(landkode: String? = "nor"): KodeverkDto {
    return if (landkode == null) KodeverkDto(betydninger = emptyMap())
    else
        KodeverkDto(
            betydninger = mapOf(
                landkode to listOf(
                    BetydningDto(
                        gyldigFra = LocalDate.of(2024, 1, 1),
                        gyldigTil = LocalDate.now().plusYears(10),
                        beskrivelser = mapOf(
                            "nb" to BeskrivelseDto(
                                term = "Norge",
                                tekst = "Norge",
                            )
                        )
                    )
                )
            )
        )
}
