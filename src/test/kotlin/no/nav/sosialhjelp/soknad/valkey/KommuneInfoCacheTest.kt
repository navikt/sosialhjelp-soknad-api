package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.Kontaktpersoner
import no.nav.sosialhjelp.soknad.app.config.CustomCacheErrorHandler
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoCacheConfig
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.SimpleKey

class KommuneInfoCacheTest : AbstractCacheTest(KommuneInfoCacheConfig.CACHE_NAME) {
    @Autowired
    private lateinit var kommuneInfoService: KommuneInfoService

    @MockkBean
    private lateinit var kommuneInfoClient: KommuneInfoClient

    @Test
    override fun `Verdi skal lagres i cache`() {
        every { kommuneInfoClient.getAll() } returns createListOfKommuneInfo()

        kommuneInfoService.hentAlleKommuneInfo()!!
            .also { infos ->
                assertThat(infos).hasSize(2)
                assertThat(infos.values).anyMatch { it.kommunenummer == "0301" || it.kommunenummer == "9999" }
            }

        cache.get(SimpleKey.EMPTY, Map::class.java)
            .let { infoMap -> infoMap.values.map { it as KommuneInfo } }
            .also { infos ->
                assertThat(infos).hasSize(2).anyMatch { it.kommunenummer == "0301" || it.kommunenummer == "9999" }
            }

        verify(exactly = 1) { kommuneInfoClient.getAll() }
    }

    @Test
    override fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(KommuneInfoCacheConfig.CACHE_NAME) } returns cache
        every { cache.get(SimpleKey.EMPTY) } throws RuntimeException("Noe feilet")
        every { kommuneInfoClient.getAll() } returns createListOfKommuneInfo()

        kommuneInfoService.hentAlleKommuneInfo()!!
            .also { infos ->
                assertThat(infos).hasSize(2)
                assertThat(infos.values).anyMatch { it.kommunenummer == "0301" || it.kommunenummer == "9999" }
            }

        verify(exactly = 1) { kommuneInfoClient.getAll() }
        verify(exactly = 1) { cache.get(SimpleKey.EMPTY) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCacheGetError(any(), cache, SimpleKey.EMPTY) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        cache.put(SimpleKey.EMPTY, createListOfKommuneInfo().associateBy { it.kommunenummer })

        kommuneInfoService.hentAlleKommuneInfo()!!.values
            .also { infos ->
                assertThat(infos).hasSize(2)
                assertThat(infos).anyMatch { it.kommunenummer == "0301" || it.kommunenummer == "9999" }
            }

        cache.get(SimpleKey.EMPTY, Map::class.java)
            .let { infoMap -> infoMap.values.map { it as KommuneInfo } }
            .also { infos ->
                assertThat(infos).hasSize(2).anyMatch { it.kommunenummer == "0301" || it.kommunenummer == "9999" }
            }

        verify(exactly = 0) { kommuneInfoClient.getAll() }
    }

    @Test
    override fun `Hvis put til cache feiler skal fortsatt innhentet verdi returneres`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(KommuneInfoCacheConfig.CACHE_NAME) } returns cache
        every { cache.put(SimpleKey.EMPTY, any()) } throws RuntimeException("Noe feilet")
        every { kommuneInfoClient.getAll() } returns createListOfKommuneInfo()

        kommuneInfoService.hentAlleKommuneInfo()!!
            .also { infos ->
                assertThat(infos).hasSize(2)
                assertThat(infos.values).anyMatch { it.kommunenummer == "0301" || it.kommunenummer == "9999" }
            }

        cache.get(SimpleKey.EMPTY, Map::class.java).also { assertThat(it).isNull() }

        verify(exactly = 1) { kommuneInfoClient.getAll() }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCachePutError(any(), cache, SimpleKey.EMPTY, any()) }
        verify(exactly = 1) { cache.put(SimpleKey.EMPTY, any()) }
        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    fun `Skal ikke lagre tom liste i cache`() {
        every { kommuneInfoClient.getAll() } returns emptyList()

        kommuneInfoService.hentAlleKommuneInfo()
            .also { infos -> assertThat(infos).isNull() }

        cache.get(SimpleKey.EMPTY, Map::class.java)
            .also { infos -> assertThat(infos).isNull() }

        verify(exactly = 1) { kommuneInfoClient.getAll() }
    }
}

private fun createListOfKommuneInfo(): List<KommuneInfo> {
    return listOf(
        KommuneInfo(
            "0301",
            true,
            true,
            false,
            false,
            kontaktpersoner = createKontaktPersoner(),
            true,
            "Oslo",
        ),
        KommuneInfo(
            "9999",
            true,
            true,
            false,
            false,
            kontaktpersoner = createKontaktPersoner(),
            true,
            "Diger kommune",
        ),
    )
}

private fun createKontaktPersoner(): Kontaktpersoner {
    return Kontaktpersoner(
        fagansvarligEpost = listOf("en@epost.no"),
        tekniskAnsvarligEpost = listOf("annen@epost.no"),
    )
}
