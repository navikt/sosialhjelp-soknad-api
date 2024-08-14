package no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle

import io.mockk.verify
import no.nav.sosialhjelp.soknad.v2.OpprettetSoknadDto
import no.nav.sosialhjelp.soknad.v2.SoknadSendtDto
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID

class LifecycleIntegrationTest : SetupLifecycleIntegrationTest() {
    @Autowired
    private lateinit var eierRepository: EierRepository

    @Autowired
    private lateinit var kontaktRepository: KontaktRepository

    @Autowired
    private lateinit var familieRepository: FamilieRepository

    @Test
    fun `Opprette soknad skal generere soknads-objekt og hente register-data`() {
        doPost(
            uri = postUri,
            responseBodyClass = OpprettetSoknadDto::class.java,
        )
            .also { assertThat(it.soknadId).isInstanceOf(UUID::class.java) }
            .soknadId
            .also { soknadId ->
                assertThat(soknadRepository.findByIdOrNull(soknadId)).isNotNull
                assertThat(
                    soknadRepository.findByIdOrNull(soknadId)!!.tidspunkt.opprettet.isAfter(LocalDateTime.now().minusMinutes(1)),
                ).isTrue()
                assertThat(eierRepository.findByIdOrNull(soknadId)).isNotNull
                assertThat(kontaktRepository.findByIdOrNull(soknadId)).isNotNull
                assertThat(familieRepository.findByIdOrNull(soknadId)).isNotNull
            }
    }

    @Test
    fun `Slette soknad skal fjerne soknad`() {
//        val soknadId = doPost(
//            uri = postUri,
//            responseBodyClass = OpprettetSoknadDto::class.java
//        ).soknadId

        val soknadId = opprettSoknad().let { soknadRepository.save(it) }.id

        doDelete(uri = deleteUri(soknadId), soknadId)

        assertThat(soknadRepository.findByIdOrNull(soknadId)).isNull()
        assertThat(eierRepository.findByIdOrNull(soknadId)).isNull()
        assertThat(familieRepository.findByIdOrNull(soknadId)).isNull()
        assertThat(kontaktRepository.findByIdOrNull(soknadId)).isNull()

        verify(exactly = 1) { mellomlagringService.deleteAll(any()) }
    }

    @Test
    fun `Sende soknad skal avslutte soknad i db`() {
        val soknadId = opprettSoknad().let { soknadRepository.save(it) }.id

        doPost(
            uri = sendUri(soknadId),
            responseBodyClass = SoknadSendtDto::class.java,
            soknadId = soknadId,
        )
    }

    // TODO Hvis det kastes exception underveis i opprettelsen - sjekk at ingenting lagres

    companion object {
        private val postUri = "/soknad/create"

        private fun deleteUri(soknadId: UUID) = "/soknad/$soknadId/delete"

        private fun sendUri(soknadId: UUID) = "/soknad/$soknadId/send"
    }
}
