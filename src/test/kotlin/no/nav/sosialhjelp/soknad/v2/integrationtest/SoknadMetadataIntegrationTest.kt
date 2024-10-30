package no.nav.sosialhjelp.soknad.v2.integrationtest

import io.mockk.every
import no.nav.sosialhjelp.soknad.v2.SoknadSendtDto
import no.nav.sosialhjelp.soknad.v2.StartSoknadResponseDto
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.util.UUID

@AutoConfigureWebTestClient(timeout = "36000")
class SoknadMetadataIntegrationTest : SetupLifecycleIntegrationTest() {
    @Test
    fun `Skal opprette metadata ved start av soknad`() {
        opprettSoknadMedEierOgKontaktForInnsending()
            .let { soknadMetadataRepository.findByIdOrNull(it) }
            .also { assertThat(it).isNotNull() }
    }

    @Test
    fun `Skal oppdatere metadata ved innsending av soknad`() {
        val uuid = opprettSoknadMedEierOgKontaktForInnsending()

        doPost(
            uri = sendUrl(uuid),
            responseBodyClass = SoknadSendtDto::class.java,
            soknadId = uuid,
        )

        soknadMetadataRepository.findByIdOrNull(uuid)!!
            .also {
                assertThat(it.innsendt!!.toLocalDate()).isEqualTo(LocalDate.now())
                assertThat(it.mottaker!!.kommunenummer).isEqualTo("0301")
            }
    }

    @Test
    fun `Skal slette metadata ved sletting av soknad`() {
    }

    private fun opprettSoknadMedEierOgKontaktForInnsending(): UUID {
        every { digisosApiV2Client.getSoknader(any()) } returns listOf()

        val (soknadId, _) =
            doPost(
                uri = createUrl(),
                responseBodyClass = StartSoknadResponseDto::class.java,
            )

        opprettEier(soknadId).also { eierRepository.save(it) }
        opprettKontakt(soknadId).also { kontaktRepository.save(it) }

        return soknadId
    }

    companion object {
        private fun createUrl() = "/soknad/create"

        private fun sendUrl(soknadId: UUID) = "/soknad/$soknadId/send"

        private fun deleteUrl(soknadId: UUID) = "/soknad/$soknadId/delete"
    }
}
