package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.config.CustomCacheErrorHandler
import no.nav.sosialhjelp.soknad.navenhet.GeografiskTilknytning
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetDto
import no.nav.sosialhjelp.soknad.navenhet.NorgCacheConfiguration
import no.nav.sosialhjelp.soknad.navenhet.NorgClient
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache

class NorgCacheTest : AbstractCacheTest(NorgCacheConfiguration.CACHE_NAME) {
    @MockkBean
    private lateinit var norgClient: NorgClient

    @Autowired
    private lateinit var norgService: NorgService

    @Test
    override fun `Verdi skal lagres i cache`() {
        val dto = NavEnhetDto("Navenhet", "12341234")
        val gt = "0301"

        every { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) } returns dto

        norgService.getEnhetForGt(gt)!!
            .also {
                assertThat(it.enhetsnavn).isEqualTo(dto.navn)
                assertThat(it.enhetsnummer).isEqualTo(dto.enhetNr)
            }

        cache.get(gt, NavEnhet::class.java)!!
            .also {
                assertThat(it.enhetsnavn).isEqualTo(dto.navn)
                assertThat(it.enhetsnummer).isEqualTo(dto.enhetNr)
            }

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }
    }

    @Test
    fun `Returneres null fra client skal ingenting lagres i cache`() {
        val gt = "0301"
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) } returns null

        norgService.getEnhetForGt(gt).also { assertThat(it).isNull() }

        cache.get(gt).also { assertThat(it).isNull() }
    }

    @Test
    fun `Ved TjenesteUtilgejngeligException skal ingenting lagres i cache`() {
        val gt = "0301"
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) } throws
            TjenesteUtilgjengeligException("Utilgjengelig", RuntimeException())

        assertThatThrownBy { norgService.getEnhetForGt(gt) }
            .isInstanceOf(TjenesteUtilgjengeligException::class.java)

        cache.get(gt).also { assertThat(it).isNull() }
    }

    @Test
    override fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`() {
        val cache: Cache = spyk()
        val gt = "0301"
        every { cacheManager.getCache(NorgCacheConfiguration.CACHE_NAME) } returns cache
        every { cache.get(gt) } throws RuntimeException("Something wrong")
        every { cache.name } returns NorgCacheConfiguration.CACHE_NAME
        mockkObject(CustomCacheErrorHandler)

        val dto = NavEnhetDto("Navenhet", "12341234")

        every { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) } returns dto

        norgService.getEnhetForGt(gt)!!.also {
            assertThat(it.enhetsnavn).isEqualTo(dto.navn)
            assertThat(it.enhetsnummer).isEqualTo(dto.enhetNr)
        }

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }
        verify(exactly = 1) { cache.get(gt) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCacheGetError(any(), cache, gt) }
        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        val gt = "0301"

        val cachetNavEnhet =
            NavEnhet("Sandvika Nav-senter", "123456", "0301", "12345678", "Sandvika")
                .also { cache.put(gt, it) }

        norgService.getEnhetForGt(gt).also { assertThat(it).isEqualTo(cachetNavEnhet) }

        verify(exactly = 0) { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }
    }

    @Test
    override fun `Hvis put til cache feiler skal fortsatt innhentet verdi returneres`() {
        val dto = NavEnhetDto("Navenhet", "12341234")
        val gt = "0301"

        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(NorgCacheConfiguration.CACHE_NAME) } returns cache
        every { cache.put(any(), any()) } throws RuntimeException("Something wrong")
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) } returns dto

        norgService.getEnhetForGt(gt)!!
            .also {
                assertThat(it.enhetsnavn).isEqualTo(dto.navn)
                assertThat(it.enhetsnummer).isEqualTo(dto.enhetNr)
            }

        cache.get(gt, NavEnhet::class.java).also { assertThat(it).isNull() }

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCachePutError(any(), cache, gt, any<NavEnhet>()) }

        unmockkObject(CustomCacheErrorHandler)
    }
}
