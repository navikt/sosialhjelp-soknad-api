package no.nav.sosialhjelp.soknad.innsending.svarut

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Status
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class OppgaveHandtererImplTest {
    private val fiksHandterer: FiksHandterer = mockk()
    private val oppgaveRepository: OppgaveRepository = mockk()

    private val oppgaveHandterer = OppgaveHandtererImpl(fiksHandterer, oppgaveRepository, schedulerDisabled = false)

    private val oppgaveSlot = slot<Oppgave>()

    @Test
    fun prosessereFeilendeOppgaveSkalSetteNesteForsok() {
        val oppgave = Oppgave(
            id = 0L,
            behandlingsId = "behandlingsId",
            type = FiksHandterer.FIKS_OPPGAVE,
            status = Status.UNDER_ARBEID,
            steg = 21,
            opprettet = LocalDateTime.now(),
            sistKjort = null,
            nesteForsok = LocalDateTime.now(),
            retries = 0
        )

        every { oppgaveRepository.hentNeste() } returns oppgave andThen null
        every { fiksHandterer.eksekver(oppgave) } throws IllegalStateException()
        every { oppgaveRepository.oppdater(capture(oppgaveSlot)) } just runs

        oppgaveHandterer.prosesserOppgaver()

        verify(exactly = 1) { oppgaveRepository.oppdater(oppgaveSlot.captured) }
        Assertions.assertThat(oppgaveSlot.captured.status).isEqualTo(Status.KLAR)
        Assertions.assertThat(oppgaveSlot.captured.nesteForsok).isNotNull
    }
}
