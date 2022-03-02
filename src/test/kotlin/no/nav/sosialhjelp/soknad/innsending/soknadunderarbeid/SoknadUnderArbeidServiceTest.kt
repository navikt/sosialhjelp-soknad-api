package no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadUnderArbeidServiceTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val soknadUnderArbeidService = SoknadUnderArbeidService(soknadUnderArbeidRepository)

    @Test
    fun settInnsendingstidspunktPaSoknadSkalHandtereEttersendelse() {
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(lagSoknadUnderArbeidForEttersendelse())
    }

    private fun lagSoknadUnderArbeidForEttersendelse(): SoknadUnderArbeid {
        return SoknadUnderArbeid()
            .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
            .withBehandlingsId(BEHANDLINGSID)
            .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
            .withEier(EIER)
            .withOpprettetDato(OPPRETTET_DATO)
            .withSistEndretDato(SIST_ENDRET_DATO)
    }

    companion object {
        private const val EIER = "12345678910"
        private const val SOKNAD_UNDER_ARBEID_ID = 1L
        private const val BEHANDLINGSID = "1100001L"
        private const val TILKNYTTET_BEHANDLINGSID = "1100002K"
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50)
        private val SIST_ENDRET_DATO = LocalDateTime.now()
    }
}
