package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.sosialhjelp.soknad.navenhet.GeografiskTilknytning
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetDto
import no.nav.sosialhjelp.soknad.navenhet.NorgCacheConfiguration
import no.nav.sosialhjelp.soknad.navenhet.NorgClient
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.dao.QueryTimeoutException

class NorgCacheTest : AbstractCacheTest() {
    @MockkBean
    private lateinit var norgClient: NorgClient

    @Autowired
    private lateinit var norgService: NorgService

    private lateinit var cache: Cache

    @BeforeEach
    fun setUp() {
        cache = cacheManager.getCache(NorgCacheConfiguration.CACHE_NAME)!!
        cache.clear()
    }

    @Test
    override fun `Verdi skal lagres i cache`() {
        val dto = NavEnhetDto("Navenhet", "12341234")
        val gt = GeografiskTilknytning("0301")

        every { norgClient.hentNavEnhetForGeografiskTilknytning(gt) } returns dto

        norgService.getEnhetForGt(gt)

        cache.get(gt.value, NavEnhet::class.java)!!
            .also {
                assertThat(it.enhetsnavn).isEqualTo(dto.navn)
                assertThat(it.enhetsnummer).isEqualTo(dto.enhetNr)
            }

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(gt) }
    }

    @Test
    fun `Returneres null fra client skal ingenting lagres i cache`() {
        val gt = GeografiskTilknytning("0301")
        every { norgClient.hentNavEnhetForGeografiskTilknytning(gt) } returns null

        norgService.getEnhetForGt(gt).also { assertThat(it).isNull() }

        cache.get(gt.value).also { assertThat(it).isNull() }
    }

    @Test
    fun `Ved TjenesteUtilgejngeligException skal ingenting lagres i cache`() {
        val gt = GeografiskTilknytning("0301")
        every { norgClient.hentNavEnhetForGeografiskTilknytning(gt) } throws TjenesteUtilgjengeligException("Utilgjengelig", RuntimeException())

        assertThatThrownBy { norgService.getEnhetForGt(gt) }
            .isInstanceOf(TjenesteUtilgjengeligException::class.java)

        cache.get(gt.value).also { assertThat(it).isNull() }
    }

    @Test
    override fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`() {
        ValkeyContainer.stopContainer()

        val dto = NavEnhetDto("Navenhet", "12341234")
        val gt = GeografiskTilknytning("0301")

        every { norgClient.hentNavEnhetForGeografiskTilknytning(gt) } returns dto

        norgService.getEnhetForGt(gt)!!
            .also {
                assertThat(it.enhetsnavn).isEqualTo(dto.navn)
                assertThat(it.enhetsnummer).isEqualTo(dto.enhetNr)
            }

        assertThatThrownBy { cache.get(gt.value, NavEnhet::class.java) }
            .isInstanceOf(QueryTimeoutException::class.java)

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(gt) }

        ValkeyContainer.startContainer()
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        val gt = GeografiskTilknytning("0301")

        val cachetNavEnhet =
            NavEnhet("Sandvika Nav-senter", "123456", "0301", "12345678", "Sandvika")
                .also { cache.put(gt.value, it) }

        norgService.getEnhetForGt(gt).also { assertThat(it).isEqualTo(cachetNavEnhet) }

        verify(exactly = 0) { norgClient.hentNavEnhetForGeografiskTilknytning(gt) }
    }
}
