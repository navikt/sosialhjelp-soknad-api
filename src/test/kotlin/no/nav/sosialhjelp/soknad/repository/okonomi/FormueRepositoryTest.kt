package no.nav.sosialhjelp.soknad.repository.okonomi

import no.nav.sosialhjelp.soknad.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.domene.okonomi.FormueRepository
import no.nav.sosialhjelp.soknad.repository.RepositoryTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class FormueRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var formueRepository: FormueRepository

    @Test
    fun `Lagre formue`() {
        val soknad = opprettSoknad()

        Formue(soknadId = soknad.id).also {
            formueRepository.save(it)
        }
    }
}