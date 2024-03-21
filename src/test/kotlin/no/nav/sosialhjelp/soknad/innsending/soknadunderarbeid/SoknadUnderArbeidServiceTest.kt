package no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadUnderArbeidServiceTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val soknadUnderArbeidService = SoknadUnderArbeidService(soknadUnderArbeidRepository, kommuneInfoService)

    @Test
    fun settInnsendingstidspunktPaSoknadSkalHandtereEttersendelse() {
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(lagSoknadUnderArbeidForEttersendelse())
    }

    private fun lagSoknadUnderArbeidForEttersendelse(): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            soknadId = SOKNAD_UNDER_ARBEID_ID,
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = null,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO
        )
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
