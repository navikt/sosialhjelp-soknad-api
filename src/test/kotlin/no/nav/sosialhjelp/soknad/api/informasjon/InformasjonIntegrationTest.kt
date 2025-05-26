package no.nav.sosialhjelp.soknad.api.informasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiErrorType
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class InformasjonIntegrationTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var personService: PersonService

    @BeforeEach
    override fun before() {
        every { personService.hasAdressebeskyttelse(any()) } returns false

        opprettSoknadBeforeEach = false
        super.before()
    }

    @Test
    fun `Hvis person ikke har adressebeskyttelse, skal alle soknader returneres`() {
        val savedIds =
            listOf(
                createAndSaveSoknad(opprettet = nowWithMillis().minusDays(1)),
                createAndSaveSoknad(opprettet = nowWithMillis().minusDays(2)),
            )

        doGet(
            uri = SESSION_URL,
            responseBodyClass = SessionResponse::class.java,
        )
            .also { response ->
                assertThat(response.open)
                    .hasSize(2)
                    .allMatch { pabegynt -> savedIds.find { it == pabegynt.soknadId } != null }
            }
    }

    @Test
    fun `Hvis person har adressebeskyttelse, skal alle soknader slettes`() {
        every { personService.hasAdressebeskyttelse(any()) } returns true

        val soknadIds =
            listOf(
                createAndSaveSoknad(opprettet = nowWithMillis().minusDays(1)),
                createAndSaveSoknad(opprettet = nowWithMillis().minusDays(2)),
            )

        doGetFullResponse(uri = SESSION_URL)
            .expectStatus().isForbidden
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody
            .also { apiError -> assertThat(apiError?.error?.name).isEqualTo(SoknadApiErrorType.Forbidden.name) }

        assertThat(metadataRepository.findAllById(soknadIds)).isEmpty()
    }

    @Test
    fun `X antall sendte soknader skal vises`() {
        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(5))
            .also { metadataRepository.save(it) }
        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(7))
            .also { metadataRepository.save(it) }

        doGet(
            uri = SESSION_URL,
            responseBodyClass = SessionResponse::class.java,
        )
            .also { response ->
                assertThat(response.userBlocked).isFalse()
                assertThat(response.numRecentlySent).isEqualTo(2)
            }
    }

    @Test
    fun `X antall sendte soknader skal vise 0 ved adressebeskyttelse`() {
        every { personService.hasAdressebeskyttelse(any()) } returns true

        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(5))
            .also { metadataRepository.save(it) }
        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(7))
            .also { metadataRepository.save(it) }

        doGetFullResponse(
            uri = SESSION_URL,
        )
            .expectStatus().isForbidden
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody
            .also { response ->
                assertThat(response.error).isEqualTo(SoknadApiErrorType.Forbidden)
            }
    }

    // TODO AdresseForslag-tester

    private fun createAndSaveSoknad(
        status: SoknadStatus = SoknadStatus.OPPRETTET,
        opprettet: LocalDateTime = nowWithMillis(),
    ): UUID {
        return opprettSoknadMetadata(status = status, opprettetDato = opprettet)
            .also { metadataRepository.save(it) }
            .also { soknadRepository.save(opprettSoknad(id = it.soknadId)) }
            .soknadId
    }

    companion object {
        const val SESSION_URL = "/informasjon/session"
    }
}
