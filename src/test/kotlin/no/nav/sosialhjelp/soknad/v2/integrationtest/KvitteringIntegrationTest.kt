package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.TOKENX
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksReadClient
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.issueToken
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.UUID

class KvitteringIntegrationTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var fiksReadClient: FiksReadClient

    private val digisosId = UUID.randomUUID()

    @Test
    fun `innsendt soknad for innlogget bruker gir 200`() {
        every { fiksReadClient.getDigisosSak(digisosId) } returns sak()

        doGetFullResponse("/kvittering/$digisosId")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.digisosId").isEqualTo(digisosId.toString())
            .jsonPath("$.tidspunktSendt").isEqualTo("2026-01-15T11:00:00")
            .jsonPath("$.filnavn").isEqualTo("Soknad.pdf")
            .jsonPath("$.soknadPdfUrl").value<String> { assert(it.endsWith("/dokumentlager/nedlasting/niva4/dok-1")) }
    }

    @Test
    fun `soknad med feil id gir 404`() {
        every { fiksReadClient.getDigisosSak(digisosId) } returns sak(fnr = "99999999999")

        doGetFullResponse("/kvittering/$digisosId").expectStatus().isNotFound
    }

    @Test
    fun `soknad null gir 404`() {
        every { fiksReadClient.getDigisosSak(digisosId) } returns sak(original = null)

        doGetFullResponse("/kvittering/$digisosId").expectStatus().isNotFound
    }

    @Test
    fun `Fiks 403 eller 404 gir 404`() {
        every { fiksReadClient.getDigisosSak(digisosId) } throws IkkeFunnetException("Fiks ga 403 for digisosId=$digisosId")

        doGetFullResponse("/kvittering/$digisosId").expectStatus().isNotFound
    }

    @Test
    fun `ugyldig digisosId gir 400`() {
        doGetFullResponse("/kvittering/ikke-en-uuid").expectStatus().isBadRequest
    }

    @Test
    fun `uten token gir 401`() {
        webTestClient
            .get()
            .uri("/kvittering/$digisosId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `token fra annen issuer gir 401`() {
        webTestClient
            .get()
            .uri("/kvittering/$digisosId")
            .header(
                HttpHeaders.AUTHORIZATION,
                BEARER +
                    issueToken(
                        mockOAuth2Server,
                        userId,
                        issuer = TOKENX,
                        audience = "localhost:teamdigisos:sosialhjelp-soknad-api",
                    ).serialize(),
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    private fun sak(
        fiksDigisosId: String = digisosId.toString(),
        fnr: String = userId,
        original: OriginalSoknadNAV? =
            OriginalSoknadNAV(
                UUID.randomUUID().toString(),
                "meta",
                "vmeta",
                DokumentInfo("Soknad.pdf", "dok-1", 1234L),
                emptyList(),
                1768471200000L,
            ),
    ) = DigisosSak(fiksDigisosId, fnr, "org", "0301", 0L, original, null, null, null)
}
