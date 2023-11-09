package no.nav.sosialhjelp.soknad.nymodell.repository.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.repository.FormueRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

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