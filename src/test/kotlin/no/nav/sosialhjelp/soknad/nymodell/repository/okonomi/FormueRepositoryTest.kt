package no.nav.sosialhjelp.soknad.nymodell.repository.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FormueRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var formueRepository: FormueRepository

    @Test
    fun `Lagre formue`() {
        val soknad = opprettSoknad()

        Formue(soknadId = soknad.id, type = FormueType.ANNET).also {
            formueRepository.save(it)
        }
    }
}