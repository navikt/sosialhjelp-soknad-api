package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.config.CustomCacheErrorHandler
import no.nav.sosialhjelp.soknad.personalia.person.AdressebeskyttelseCacheConfig
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.AdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
class AdressebeskyttelseCacheTest : AbstractCacheTest() {
    @SpykBean
    protected lateinit var cacheManager: CacheManager

    private val cache get() = cacheManager.getCache(AdressebeskyttelseCacheConfig.CACHE_NAME)!!

    @Autowired
    private lateinit var personService: PersonService

    @MockkBean
    private lateinit var hentPersonClient: HentPersonClient

    @BeforeEach
    fun setupAdressebeskyttelse() {
        cache.clear()
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns setAdressebeskyttelse(Gradering.UGRADERT)
    }

    @Test
    override fun `Verdi skal lagres i cache`() {
        personService.hasAdressebeskyttelse(USER_ID)
        verify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse(any()) }

        cache.getBoolean(USER_ID).also { assertThat(it).isFalse() }
    }

    @Test
    override fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(AdressebeskyttelseCacheConfig.CACHE_NAME) } returns cache
        every { cache.get(any()) } throws RuntimeException("Something wrong")

        personService.hasAdressebeskyttelse(USER_ID)
            .also { assertThat(it).isFalse() }

        verify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse(any()) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCacheGetError(any(), cache, USER_ID) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        cache.put(USER_ID, false)

        personService.hasAdressebeskyttelse(USER_ID).also { assertThat(it).isFalse() }
        verify(exactly = 0) { hentPersonClient.hentAdressebeskyttelse(any()) }
    }

    @Test
    override fun `Hvis put til cache feiler skal fortsatt innhentet verdi returneres`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(AdressebeskyttelseCacheConfig.CACHE_NAME) } returns cache
        every { cache.put(USER_ID, any<Boolean>()) } throws RuntimeException("Something wrong")

        personService.hasAdressebeskyttelse(USER_ID)
            .also { assertThat(it).isFalse() }

        verify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse(any()) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCachePutError(any(), cache, USER_ID, any<Boolean>()) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    fun `HasAdressebeskyttelse == true skal ikke caches`() {
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns setAdressebeskyttelse(Gradering.STRENGT_FORTROLIG)

        personService.hasAdressebeskyttelse(USER_ID)
            .also { assertThat(it).isTrue() }

        assertThat(cache.get(USER_ID)).isNull()
    }

    @Test
    fun `OnSendSoknad skal evicte value fra cache`() {
        personService.hasAdressebeskyttelse(USER_ID)
        verify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse(any()) }

        assertThat(personService.hasAdressebeskyttelse(USER_ID)).isFalse()
        verify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse(any()) }

        personService.onSendSoknadHasAdressebeskyttelse(USER_ID)
            .also { assertThat(it).isFalse() }
        verify(exactly = 2) { hentPersonClient.hentAdressebeskyttelse(any()) }

        personService.hasAdressebeskyttelse(USER_ID)
        verify(exactly = 3) { hentPersonClient.hentAdressebeskyttelse(any()) }
    }

    companion object {
        private const val USER_ID = "12345612345"
    }
}

private fun setAdressebeskyttelse(gradering: Gradering) =
    PersonAdressebeskyttelseDto(
        adressebeskyttelse = listOf(AdressebeskyttelseDto(gradering)),
    )

private fun Cache.getBoolean(key: String) = get(key)?.get() as? Boolean
