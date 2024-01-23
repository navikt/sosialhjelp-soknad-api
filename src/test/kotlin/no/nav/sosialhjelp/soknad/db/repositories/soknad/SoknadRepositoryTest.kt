package no.nav.sosialhjelp.soknad.db.repositories.soknad

import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.Navn
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SoknadRepositoryTest {

    @Autowired
    private lateinit var soknadRepository: SoknadRepository

    @Test
    fun `Lagre ny soknad`() {
        soknadRepository.save(lagTestSoknad()).let {
            assertThat(it.id).isNotNull()
        }
    }

    fun lagTestSoknad(): Soknad {
        return Soknad(
            eier = Eier(
                personId = "1234567890",
                navn = Navn(
                    fornavn = "Test",
                    etternavn = "Testesen"
                )
            ),
            innsendingstidspunkt = LocalDateTime.now()
        )
    }
}
