package no.nav.sosialhjelp.soknad.db.repositories

import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.Navn
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test", "test-container")
abstract class AbstractRepositoryTest {

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    fun opprettSoknad(
        personId: String = "1234567890",
        fornavn: String = "Test",
        etternavn: String = "Testesen",
    ): Soknad {
        return Soknad(
            eier = Eier(
                personId = personId,
                navn = Navn(
                    fornavn = fornavn,
                    etternavn = etternavn
                )
            ),
            innsendingstidspunkt = LocalDateTime.now()
        ).run {
            soknadRepository.save(this)
        }
    }
}
