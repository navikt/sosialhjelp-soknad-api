package no.nav.sosialhjelp.soknad.nymodell.repository.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseRepository
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BekreftelseRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var bekreftelseRepository: BekreftelseRepository

    @Test
    fun `Lagre bekreftelse`() {
        val soknad = opprettSoknad()

        Bekreftelse(soknadId = soknad.id).also {
            bekreftelseRepository.save(it)
        }
    }
}