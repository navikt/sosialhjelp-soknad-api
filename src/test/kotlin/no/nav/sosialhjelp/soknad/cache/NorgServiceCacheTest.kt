package no.nav.sosialhjelp.soknad.cache

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import io.mockk.verify
import no.nav.sosialhjelp.soknad.navenhet.GeografiskTilknytning
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetDto
import no.nav.sosialhjelp.soknad.navenhet.NorgCacheConfig
import no.nav.sosialhjelp.soknad.navenhet.NorgClient
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager

class NorgServiceCacheTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var norgService: NorgService

    @Autowired
    private lateinit var cacheManager: CacheManager

    @MockkSpyBean
    private lateinit var norgClient: NorgClient


    @Test
    fun `NorgService skal cache med riktig key`() {

        val gt = "0301"

        every { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) } returns createNavEnhetDto()

        val enhetForGt = norgService.getEnhetForGt("0301")

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }

        cacheManager.getCache(NorgCacheConfig.CACHE_NAME)
            .let { cache -> cache!!.get("0301") }
            .let { wrapper -> wrapper!!.get() as NavEnhet }
            .also { navEnhet ->
                assertThat(navEnhet.enhetsnavn).isEqualTo(enhetForGt!!.enhetsnavn)
                assertThat(navEnhet.enhetsnummer).isEqualTo(enhetForGt.enhetsnummer)
            }
        // markere at norgClient ikke kalles på runde 2
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) } returns null

        norgService.getEnhetForGt("0301")
            ?.also {
                assertThat(it.enhetsnavn).isEqualTo(enhetForGt!!.enhetsnavn)
                assertThat(it.enhetsnummer).isEqualTo(enhetForGt.enhetsnummer)
            }

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }
    }
}

private fun createNavEnhetDto(): NavEnhetDto {
    return NavEnhetDto(
        navn = "NavEnhet",
        enhetNr = "1234",
    )
}
