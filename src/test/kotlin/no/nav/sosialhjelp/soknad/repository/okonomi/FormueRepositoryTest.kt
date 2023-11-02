package no.nav.sosialhjelp.soknad.repository.okonomi

import no.nav.sosialhjelp.soknad.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.domene.okonomi.FormueRepository
import no.nav.sosialhjelp.soknad.domene.okonomi.type.FormueType
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

        Formue(soknadId = soknad.id, type = FormueType.KONTOOVERSIKT_ANNET).also {
            formueRepository.save(it)
        }
    }
}