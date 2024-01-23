package no.nav.sosialhjelp.soknad.db.repositories.soknad

import no.nav.sosialhjelp.soknad.db.repositories.AbstractRepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SoknadRepositoryTest: AbstractRepositoryTest() {

    @Test
    fun `Lagre ny soknad`() {
        opprettSoknad().let {
            assertThat(it.id).isNotNull()
        }
    }
}
