package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractRegisterDataTest {
    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    protected lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = soknadRepository.save(opprettSoknad())

        val staticSubjectHandlerImpl = StaticSubjectHandlerImpl().apply { setUser(soknad.eierPersonId) }
        SubjectHandlerUtils.setNewSubjectHandlerImpl(staticSubjectHandlerImpl)
    }
}
