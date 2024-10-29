package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.getunleash.Unleash
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.SoknadSendtDto
import no.nav.sosialhjelp.soknad.v2.StartSoknadResponseDto
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class SoknadIntegrationTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    @SpykBean
    private lateinit var unleash: Unleash

    @MockkBean
    private lateinit var digisosApiV2Client: DigisosApiV2Client

    @BeforeEach
    fun setup() {
        clearAllMocks()
        soknadRepository.deleteAll()
        every { mellomlagringClient.deleteAllVedlegg(any()) } just runs
        every { unleash.isEnabled("sosialhjelp.soknad.kort_soknad", false) } returns true
    }

    @Test
    fun `Opprett soknad skal ikke bli kort hvis bruker ikke har sendt inn soknad de siste 120 dager og ikke har nylige utbetalinger`() {
        every { digisosApiV2Client.getSoknader(any()) } returns listOf()
        val (id, useKortSoknad) =
            webTestClient
                .post()
                .uri("/soknad/create")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "BEARER ${token.serialize()}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(StartSoknadResponseDto::class.java)
                .returnResult()
                .responseBody!!

        assertThat(id).isNotNull()
        assertThat(useKortSoknad).isFalse()
        val soknad = soknadRepository.findById(id)
        assertThat(soknad).isPresent()
        assertThat(soknad.get().kortSoknad).isFalse()
    }

    @Test
    fun `Skal slette lagret soknad`() {
        val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }

        webTestClient
            .delete()
            .uri("/soknad/$lagretSoknadId/delete")
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
            .uri("/soknad/$randomUUID/delete")
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
                assertThat(it.message).isEqualTo("NyModell: Soknad finnes ikke")
            }
    }

    @Test
    fun `Skal opprette innsendtSoknadMetadata ved start av soknad`() {
        every { digisosApiV2Client.getSoknader(any()) } returns listOf()

        val response =
            doPost(
                uri = "/soknad/opprettSoknad",
                responseBodyClass = StartSoknadResponseDto::class.java,
            )

        val innsendtSoknadMetadata = innsendtSoknadMetadataRepository.findById(response.soknadId)
        assertThat(innsendtSoknadMetadata).isPresent()
    }

    @Test
    fun `skal oppdatere innsendtSoknadMetadata med innsendt_dato ved innsending av soknad`() {
        val soknadId = opprettSoknadMedEierOgKontaktForInnsending()
        val soknad = soknadRepository.findById(soknadId).get()

        doPost(
            uri = "/soknad/$soknadId/send",
            responseBodyClass = SoknadSendtDto::class.java,
            soknadId = soknadId,
        )

        val innsendtSoknadMetadata = innsendtSoknadMetadataRepository.findById(soknadId)
        assertThat(innsendtSoknadMetadata).isPresent()
        assertThat(innsendtSoknadMetadata.get().sendt_inn_dato).isEqualTo(soknad.tidspunkt.sendtInn)
    }

    private fun opprettSoknadMedEierOgKontaktForInnsending(): UUID {
        every { digisosApiV2Client.getSoknader(any()) } returns listOf()

        val (soknadId, _) =
            doPost(
                uri = "/soknad/opprettSoknad",
                responseBodyClass = StartSoknadResponseDto::class.java,
            )

        opprettEier(soknadId).also { eierRepository.save(it) }
        opprettKontakt(soknadId).also { kontaktRepository.save(it) }

        return soknadId
    }
}
