package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.getunleash.Unleash
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.v2.StartSoknadResponseDto
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@AutoConfigureWebTestClient(timeout = "36000")
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
        every { digisosApiV2Client.krypterOgLastOppFiler(any(), any(), any(), any(), any(), any(), any()) } returns UUID.randomUUID().toString()
        every { unleash.isEnabled("sosialhjelp.soknad.kort_soknad", false) } returns true
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns
            MellomlagringDto(UUID.randomUUID().toString(), mellomlagringMetadataList = emptyList())
    }

    @Test
    fun `Opprett soknad skal ikke bli kort hvis bruker ikke har sendt inn soknad de siste 120 dager og ikke har nylige utbetalinger`() {
        every { digisosApiV2Client.getSoknader(any()) } returns listOf()

        val (id, useKortSoknad) =
            doPost(
                uri = createUrl(),
                responseBodyClass = StartSoknadResponseDto::class.java,
            )

        assertThat(id).isNotNull()
        assertThat(useKortSoknad).isFalse()
        val soknad = soknadRepository.findById(id)
        assertThat(soknad).isPresent()
        assertThat(soknad.get().kortSoknad).isFalse()
    }

    @Test
    fun `Skal slette lagret soknad`() {
        val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }

        doDelete(
            uri = "/soknad/$lagretSoknadId/delete",
            soknadId = lagretSoknadId,
        )
            .expectStatus().isNoContent

        assertThat(soknadRepository.findById(lagretSoknadId).getOrNull()).isNull()
    }

    @Test
    fun `Slette soknad som ikke finnes skal gi 404`() {
        val randomUUID = UUID.randomUUID()

        doDelete(
            uri = "/soknad/$randomUUID/delete",
            soknadId = randomUUID,
        )
            .expectStatus().isNotFound
    }

    companion object {
        private fun createUrl() = "/soknad/create"

        private fun sendUrl(soknadId: UUID) = "/soknad/$soknadId/send"
    }
}
