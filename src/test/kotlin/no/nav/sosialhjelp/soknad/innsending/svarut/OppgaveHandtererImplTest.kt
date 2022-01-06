package no.nav.sosialhjelp.soknad.innsending.svarut

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.business.db.repositories.oppgave.OppgaveRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Optional

internal class OppgaveHandtererImplTest {
    private val fiksHandterer: FiksHandterer = mockk()
    private val oppgaveRepository: OppgaveRepository = mockk()

    private val oppgaveHandterer = OppgaveHandtererImpl(fiksHandterer, oppgaveRepository)

    private val oppgaveSlot = slot<Oppgave>()

    @Test
    fun prosessereFeilendeOppgaveSkalSetteNesteForsok() {
        val oppgave = Oppgave()
        oppgave.status = Oppgave.Status.UNDER_ARBEID

        every { oppgaveRepository.hentNeste() } returns Optional.of(oppgave) andThen Optional.empty()
        every { fiksHandterer.eksekver(oppgave) } throws IllegalStateException()
        every { oppgaveRepository.oppdater(capture(oppgaveSlot)) } just runs

        oppgaveHandterer.prosesserOppgaver()

        verify(exactly = 1) { oppgaveRepository.oppdater(oppgaveSlot.captured) }
        Assertions.assertThat(oppgaveSlot.captured.status).isEqualTo(Oppgave.Status.KLAR)
        Assertions.assertThat(oppgaveSlot.captured.nesteForsok).isNotNull
    }
}
