package no.nav.sosialhjelp.soknad.cache

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.kodeverk.BeskrivelseDto
import no.nav.sosialhjelp.soknad.kodeverk.BetydningDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkCacheConfig
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkClient
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkStore
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
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

    @Test
    fun `Hente kodeverk kommuner skal caches`() {

        every { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER) } returns createKommuner()

        val kommunenavn = kodeverkService.getKommunenavn("0301")

        val cache = cacheManager.getCache(KodeverkCacheConfig.CACHE_NAME)

        val a = 4

    }

    @Test
    fun `Hente kodeverk poststed skal caches`() {
    }


    @Test
    fun `Hente kodeverk land skal caches`() {
    }

    @Test
    fun `Hvis cache hit er null skal den evictes og det skal hentes fra source`() {

    }
}


private fun createKommuner(kommunenummer: String = "0301"): KodeverkDto {
    return KodeverkDto(
        betydninger = mapOf(
            kommunenummer to listOf(
                BetydningDto(
                    gyldigFra = LocalDate.of(2024, 1, 1),
                    gyldigTil = LocalDate.now().plusYears(10),
                    beskrivelser = mapOf(
                        "nb" to BeskrivelseDto(
                            term = "Oslo",
                            tekst = "Oslo",
                        )
                    )
                )
            )
        )
    )
}
