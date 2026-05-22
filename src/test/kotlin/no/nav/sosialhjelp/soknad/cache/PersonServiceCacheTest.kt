package no.nav.sosialhjelp.soknad.cache

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.person.AdressebeskyttelseCacheConfig.Companion.CACHE_NAME
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.AdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager

class PersonServiceCacheTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun setup() {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
        cacheManager.getCache(CACHE_NAME)?.clear()
    }

    @Test
    suspend fun `Hente adressebeskyttelse false skal caches`() {
        coEvery { hentPersonClient.hentAdressebeskyttelse() } returns createPersonAdressebeskyttelseDto()

        personService.hasAdressebeskyttelse("ident")

        cacheManager.getCache(CACHE_NAME)!!
            .get("ident")!!
            .let { it.get() as Boolean }
            .also { assertThat(it).isFalse }

        coVerify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse() }

        personService.hasAdressebeskyttelse("ident")

        coVerify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse() }
    }

    @Test
    fun `Hente adressebeskyttelse true skal ikke caches`() {
        coEvery { hentPersonClient.hentAdressebeskyttelse() } returns createPersonAdressebeskyttelseDto(Gradering.STRENGT_FORTROLIG)

        personService.hasAdressebeskyttelse("ident").also { assertThat(it).isTrue }

        assertThat(cacheManager.getCache(CACHE_NAME)!!.get("ident")).isNull()
        coVerify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse() }

        personService.hasAdressebeskyttelse("ident").also { assertThat(it).isTrue }

        assertThat(cacheManager.getCache(CACHE_NAME)!!.get("ident")).isNull()
        coVerify(exactly = 2) { hentPersonClient.hentAdressebeskyttelse() }
    }

    @Test
    fun `Ved sending skal cache evictes`() {

        coEvery { hentPersonClient.hentAdressebeskyttelse() } returns createPersonAdressebeskyttelseDto()

        personService.hasAdressebeskyttelse("ident")

        cacheManager.getCache(CACHE_NAME)!!
            .get("ident")!!
            .let { it.get() as Boolean }
            .also { assertThat(it).isFalse }

        coVerify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse() }

        personService.onSendSoknadHasAdressebeskyttelse("ident")

        coVerify(exactly = 2) { hentPersonClient.hentAdressebeskyttelse() }

        assertThat(cacheManager.getCache(CACHE_NAME)!!.get("ident")).isNull()
    }
}

private fun createPersonAdressebeskyttelseDto(gradering: Gradering = Gradering.UGRADERT) =
    PersonAdressebeskyttelseDto(
        adressebeskyttelse =
            listOf(
                AdressebeskyttelseDto(gradering),
            ),
    )
