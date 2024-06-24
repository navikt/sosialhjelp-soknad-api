package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.getunleash.Unleash
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.StartSoknadResponseDto
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.time.LocalDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class SoknadIntegrationTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    @SpykBean
    private lateinit var unleash: Unleash

    @BeforeEach
    fun setup() {
        every { mellomlagringClient.deleteAllVedlegg(any()) } just runs
        every { unleash.isEnabled("sosialhjelp.soknad.kort_soknad", false) } returns true
    }

    @Test
    fun `Opprett søknad skal bli kort hvis bruker har sendt inn søknad de siste 4 mnd`() {
        opprettSoknad(sendtInn = LocalDateTime.now().minusMonths(3)).also { soknadRepository.save(it) }
        val (id, useKortSoknad) =
            webTestClient
                .post()
                .uri("/soknad/opprettSoknad")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "BEARER ${token.serialize()}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(StartSoknadResponseDto::class.java)
                .returnResult()
                .responseBody

        assertThat(id).isNotNull()
        assertThat(useKortSoknad).isTrue()
        val soknad = soknadRepository.findById(UUID.fromString(id))
        assertThat(soknad).isPresent()
        assertThat(soknad.get().kortSoknad).isTrue()
    }

    @Test
    fun `Opprett søknad skal ikke bli kort hvis bruker ikke har sendt inn søknad de siste 4 mnd`() {
        val (id, useKortSoknad) =
            webTestClient
                .post()
                .uri("/soknad/opprettSoknad")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "BEARER ${token.serialize()}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(StartSoknadResponseDto::class.java)
                .returnResult()
                .responseBody

        assertThat(id).isNotNull()
        assertThat(useKortSoknad).isFalse()
        val soknad = soknadRepository.findById(UUID.fromString(id))
        assertThat(soknad).isPresent()
        assertThat(soknad.get().kortSoknad).isFalse()
    }

    @Test
    fun `Skal slette lagret soknad`() {
        val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }

        webTestClient
            .delete()
            .uri("/soknad/$lagretSoknadId")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "BEARER ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(lagretSoknadId.toString(), id = token.jwtClaimsSet.subject))
            .exchange()
            .expectStatus()
            .isNoContent

        assertThat(soknadRepository.findById(lagretSoknadId).getOrNull()).isNull()
    }

    @Test
    fun `Slette soknad som ikke finnes skal gi 404`() {
        val randomUUID = UUID.randomUUID()
        webTestClient
            .delete()
            .uri("/soknad/$randomUUID")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "BEARER ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(randomUUID.toString(), id = token.jwtClaimsSet.subject))
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(SoknadApiError::class.java)
            .returnResult()
            .responseBody!!
            .also {
                assertThat(it.message).isEqualTo("Ingen søknad med denne behandlingsId funnet")
            }
    }
}
