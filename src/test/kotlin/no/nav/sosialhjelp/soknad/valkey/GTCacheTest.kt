package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.CustomCacheErrorHandler
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.navenhet.gt.GTCacheConfiguration
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningClient
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import java.util.UUID

class GTCacheTest : AbstractCacheTest(GTCacheConfiguration.CACHE_NAME) {
    @Autowired
    private lateinit var gtService: GeografiskTilknytningService

    @MockkBean
    private lateinit var gtClient: GeografiskTilknytningClient

    @MockkBean
    private lateinit var soknadServiceImpl: SoknadServiceImpl

    @BeforeEach
    override fun setup() {
        super.setup()
        every { soknadServiceImpl.findPersonId(any()) } returns "12345612345"
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        val soknadId = UUID.randomUUID()
        val kommunenummer = "0301"

        runCatching {
            cacheManager.getCache(GTCacheConfiguration.CACHE_NAME)!!.put(soknadId, kommunenummer)
        }
            .onFailure {
                logger.error("Feil ved test", it)
            }
            .getOrNull()

        gtService.hentGeografiskTilknytning(soknadId)!!
            .also { assertThat(it.value).isEqualTo(kommunenummer) }

        verify(exactly = 0) { gtClient.hentGeografiskTilknytning(any()) }
    }

    @Test
    override fun `Verdi skal lagres i cache`() {
        val soknadId = UUID.randomUUID()
        val kommunenummer = "0301"

        every { gtClient.hentGeografiskTilknytning(any()) } returns
            GeografiskTilknytningDto(
                GtType.KOMMUNE,
                kommunenummer,
                null,
                "NOR",
            )

        gtService.hentGeografiskTilknytning(soknadId)!!
            .also { assertThat(it.value).isEqualTo(kommunenummer) }

        cache.get(soknadId, String::class.java)
            .also { assertThat(it).isEqualTo(kommunenummer) }

        verify(exactly = 1) { gtClient.hentGeografiskTilknytning(any()) }
    }

    @Test
    override fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(GTCacheConfiguration.CACHE_NAME) } returns cache
        every { cache.get(any()) } throws RuntimeException("Something wrong")

        val kommunenummer = "0301"

        every { gtClient.hentGeografiskTilknytning(any()) } returns
            GeografiskTilknytningDto(
                GtType.KOMMUNE,
                kommunenummer,
                null,
                "NOR",
            )

        val soknadId = UUID.randomUUID()
        gtService.hentGeografiskTilknytning(soknadId)!!
            .also { assertThat(it.value).isEqualTo(kommunenummer) }

        verify(exactly = 1) { gtClient.hentGeografiskTilknytning(any()) }
        verify(exactly = 1) { cache.get(any()) }
        verify { CustomCacheErrorHandler.handleCacheGetError(any(), cache, soknadId) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    fun `Client returnerer null skal ikke lagre noe i cache`() {
        every { gtClient.hentGeografiskTilknytning(any()) } returns null

        val soknadId = UUID.randomUUID()
        gtService.hentGeografiskTilknytning(soknadId).also { assertThat(it).isNull() }

        cache.get(soknadId).also { assertThat(it).isNull() }

        verify(exactly = 1) { gtClient.hentGeografiskTilknytning(any()) }
    }

    @Test
    fun `PdlApiException skal ikke lagre noe i cache`() {
        every { gtClient.hentGeografiskTilknytning(any()) } throws PdlApiException("Something wrong")

        val soknadId = UUID.randomUUID()
        assertThatThrownBy { gtService.hentGeografiskTilknytning(soknadId) }
            .isInstanceOf(PdlApiException::class.java)

        cache.get(soknadId).also { assertThat(it).isNull() }

        verify(exactly = 1) { gtClient.hentGeografiskTilknytning(any()) }
    }

    @Test
    fun `TjenesteUtilgjengeligException skal ikke lager noe i cache`() {
        every { gtClient.hentGeografiskTilknytning(any()) } throws
            TjenesteUtilgjengeligException("Something wrong", RuntimeException("Something wrong"))

        val soknadId = UUID.randomUUID()
        assertThatThrownBy { gtService.hentGeografiskTilknytning(soknadId) }
            .isInstanceOf(TjenesteUtilgjengeligException::class.java)

        cache.get(soknadId).also { assertThat(it).isNull() }

        verify(exactly = 1) { gtClient.hentGeografiskTilknytning(any()) }
    }

    companion object {
        val logger by logger()
    }
}
