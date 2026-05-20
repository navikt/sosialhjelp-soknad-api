package no.nav.sosialhjelp.soknad.cache

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.person.AdressebeskyttelseCacheConfig
import no.nav.sosialhjelp.soknad.personalia.person.AdressebeskyttelseCacheConfig.Companion.CACHE_NAME
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.AdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager


class PersonServiceCacheTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var cacheManager: CacheManager

    @MockkSpyBean
    override lateinit var hentPersonClient: HentPersonClient

    @BeforeEach
    fun setup() {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @Test
    suspend fun `Hente adressebeskyttelse false skal caches`() {
        coEvery { hentPersonClient.hentAdressebeskyttelse() } returns createPersonAdressebeskyttelseDto()

        personService.hasAdressebeskyttelse("ident")

        val value = cacheManager.getCache(CACHE_NAME)
            ?.get("ident")

        Assertions.assertThat(value).isNotNull

        coVerify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse() }

        personService.hasAdressebeskyttelse("ident")

        coVerify(exactly = 1) { hentPersonClient.hentAdressebeskyttelse() }

    }

    @Test
    fun `Hente adressebeskyttelse true skal ikke caches`() {

    }

    @Test
    fun `Hente adressebeskyttelse ved sending skal ikke caches`() {

    }
}

private fun createPersonAdressebeskyttelseDto(gradering: Gradering = Gradering.UGRADERT) =
    PersonAdressebeskyttelseDto(
        adressebeskyttelse = listOf(
            AdressebeskyttelseDto(gradering)
        )
    )
