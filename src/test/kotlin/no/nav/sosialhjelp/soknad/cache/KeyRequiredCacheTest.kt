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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager

class KeyRequiredCacheTest: AbstractIntegrationTest() {

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

        val cache = cacheManager.getCache(NorgCacheConfig.CACHE_NAME)
        val value = cache?.get("0301")


        val enhetForGt2 = norgService.getEnhetForGt("0301")


        verify(exactly = 1) { norgClient }
    }

}

private fun createNavEnhetDto(): NavEnhetDto {
    return NavEnhetDto(
        navn = "NavEnhet",
        enhetNr = "1234",
    )
}
