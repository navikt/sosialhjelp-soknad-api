package no.nav.sosialhjelp.soknad.nymodell.repository.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bostotte
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BostotteRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BostotteRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var bostotteRepository: BostotteRepository

    @Test
    fun `Lagre Bostotte`() {
        val nySoknad = opprettSoknad()

        Bostotte(
            soknadId = nySoknad.id,
            status = BostotteStatus.UNDER_BEHANDLING
        ).also { bostotteRepository.save(it) }

        assertThat(bostotteRepository.findAll()).hasSize(1)
    }

}