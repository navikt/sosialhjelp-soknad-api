package no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle

import io.mockk.verify
import no.nav.sosialhjelp.soknad.v2.OpprettetSoknadDto
import no.nav.sosialhjelp.soknad.v2.SoknadSendtDto
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
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
        createNewSoknad()
            .also { soknadId -> assertThat(soknadId).isInstanceOf(UUID::class.java) }
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
        val soknadId = createNewSoknad()

        doDelete(uri = deleteUri(soknadId), soknadId = soknadId)

        assertThat(soknadRepository.findByIdOrNull(soknadId)).isNull()
        assertThat(eierRepository.findByIdOrNull(soknadId)).isNull()
        assertThat(familieRepository.findByIdOrNull(soknadId)).isNull()
        assertThat(kontaktRepository.findByIdOrNull(soknadId)).isNull()

        verify(exactly = 1) { mellomlagringService.deleteAll(any()) }
    }

    @Test
    fun `Sende soknad skal avslutte soknad i db`() {
        val soknadId = createNewSoknad()

        kontaktRepository.findByIdOrNull(soknadId)!!
            .run { copy(adresser = adresser.copy(adressevalg = AdresseValg.FOLKEREGISTRERT)) }
            .also { kontaktRepository.save(it) }

        doPost(
            uri = sendUri(soknadId),
            responseBodyClass = SoknadSendtDto::class.java,
            soknadId = soknadId,
        )
    }

    // TODO Hvis det kastes exception underveis i opprettelsen - sjekk at ingenting lagres

    private fun createNewSoknad(): UUID {
        return doPost(
            uri = createUri,
            responseBodyClass = OpprettetSoknadDto::class.java,
        ).soknadId
    }

    companion object {
        private val createUri = "/soknad/create"

        private fun deleteUri(soknadId: UUID) = "/soknad/$soknadId/delete"

        private fun sendUri(soknadId: UUID) = "/soknad/$soknadId/send"
    }
}
