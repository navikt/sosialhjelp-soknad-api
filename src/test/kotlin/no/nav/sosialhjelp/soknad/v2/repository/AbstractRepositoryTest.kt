package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test", "test-container")
abstract class AbstractRepositoryTest {

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    protected lateinit var soknad: Soknad

    @BeforeEach
    fun saveSoknad() {
        soknad = soknadRepository.save(
            opprettSoknad(id = UUID.randomUUID())
        )
    }
}
