package no.nav.sosialhjelp.soknad.api.informasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
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
        every { personService.harAdressebeskyttelse(any()) } returns false

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
                    .allMatch { pabegynt -> savedIds.find { it.toString() == pabegynt.behandlingsId } != null }
            }
    }

    @Test
    fun `Hvis person har adressebeskyttelse, skal alle soknader slettes`() {
        every { personService.harAdressebeskyttelse(any()) } returns true

        val soknadIds =
            listOf(
                createAndSaveSoknad(opprettet = nowWithMillis().minusDays(1)),
                createAndSaveSoknad(opprettet = nowWithMillis().minusDays(2)),
            )

        doGet(
            uri = SESSION_URL,
            responseBodyClass = SessionResponse::class.java,
        )
            .also { response ->
                assertThat(response.userBlocked).isTrue()
                assertThat(response.open).hasSize(0)
                assertThat(response.numRecentlySent).isEqualTo(0)
            }

        assertThat(soknadMetadataRepository.findAllById(soknadIds)).isEmpty()
    }

    @Test
    fun `X antall sendte soknader skal vises`() {
        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(5))
            .also { soknadMetadataRepository.save(it) }
        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(7))
            .also { soknadMetadataRepository.save(it) }

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
        every { personService.harAdressebeskyttelse(any()) } returns true

        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(5))
            .also { soknadMetadataRepository.save(it) }
        opprettSoknadMetadata(status = SoknadStatus.SENDT, innsendtDato = nowWithMillis().minusDays(7))
            .also { soknadMetadataRepository.save(it) }

        doGet(
            uri = SESSION_URL,
            responseBodyClass = SessionResponse::class.java,
        )
            .also { response ->
                assertThat(response.userBlocked).isTrue()
                assertThat(response.numRecentlySent).isEqualTo(0)
            }
    }

    // TODO AdresseForslag-tester

    private fun createAndSaveSoknad(
        status: SoknadStatus = SoknadStatus.OPPRETTET,
        opprettet: LocalDateTime = nowWithMillis(),
    ): UUID {
        return opprettSoknadMetadata(status = status, opprettetDato = opprettet)
            .also { soknadMetadataRepository.save(it) }
            .also { soknadRepository.save(opprettSoknad(id = it.soknadId)) }
            .soknadId
    }

    companion object {
        const val SESSION_URL = "/informasjon/session"
    }
}
