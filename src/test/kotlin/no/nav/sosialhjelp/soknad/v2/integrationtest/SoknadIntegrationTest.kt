package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.getunleash.Unleash
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus.Status
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.StartSoknadResponseDto
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
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

    // TODO Flytte en del/alle disse til Lifecycle-test? Eventuelt egen for kort soknad

    @Test
    fun `Opprett soknad skal bli kort hvis bruker har sendt inn soknad de siste 120 dager`() {
        opprettSoknad(sendtInn = LocalDateTime.now().minusDays(40)).also { soknadRepository.save(it) }

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
        assertThat(useKortSoknad).isTrue()
        val soknad = soknadRepository.findById(id)
        assertThat(soknad).isPresent()
        assertThat(soknad.get().kortSoknad).isTrue()
    }

    @Test
    fun `Opprett soknad skal bli kort hvis bruker har sendt inn papirsoknad de siste 120 dager`() {
        val digisosSak =
            DigisosSak(
                "abc",
                "123",
                "abc",
                "123",
                Instant.now().toEpochMilli(),
                null,
                null,
                DigisosSoker("123", emptyList(), Instant.now().toEpochMilli()),
                null,
            )
        val digisosSoker = JsonDigisosSoker().withHendelser(listOf(JsonSoknadsStatus().withStatus(Status.MOTTATT).withHendelsestidspunkt(OffsetDateTime.now().minusDays(30).toString())))
        every { digisosApiV2Client.getSoknader(any()) } returns listOf(digisosSak)
        every { digisosApiV2Client.getInnsynsfil("abc", "123", any()) } returns digisosSoker

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
        assertThat(useKortSoknad).isTrue()
        val soknad = soknadRepository.findById(id)
        assertThat(soknad).isPresent()
        assertThat(soknad.get().kortSoknad).isTrue()
    }

    @Test
    fun `Opprett soknad skal bli kort hvis bruker nylig har fatt utbetaling`() {
        val (digisosSak, digisosSoker) = digisosSakOgSoker(listOf(JsonUtbetaling().withStatus(JsonUtbetaling.Status.UTBETALT).withUtbetalingsdato(OffsetDateTime.now().minusDays(30).toString())))
        every { digisosApiV2Client.getSoknader(any()) } returns listOf(digisosSak)
        every { digisosApiV2Client.getInnsynsfil("abc", "123", any()) } returns digisosSoker

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
        assertThat(useKortSoknad).isTrue()
        val soknad = soknadRepository.findById(id)
        assertThat(soknad).isPresent()
        assertThat(soknad.get().kortSoknad).isTrue()
    }

    private fun digisosSakOgSoker(hendelser: List<JsonHendelse> = emptyList()): Pair<DigisosSak, JsonDigisosSoker> {
        val digisosSak =
            DigisosSak(
                "abc",
                "123",
                "abc",
                "123",
                Instant.now().toEpochMilli(),
                null,
                null,
                DigisosSoker("123", emptyList(), Instant.now().toEpochMilli()),
                null,
            )
        val digisosSoker = JsonDigisosSoker().withHendelser(hendelser)
        return Pair(digisosSak, digisosSoker)
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
}
