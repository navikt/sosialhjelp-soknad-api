package no.nav.sosialhjelp.soknad.v2.register

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.UploadClient
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractRegisterDataTest {
    @MockkBean
    protected lateinit var uploadClient: UploadClient

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    @Autowired
    protected lateinit var soknadMetadataRepository: SoknadMetadataRepository

    protected lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        every { uploadClient.delete(any(), any()) } just runs

        val soknadId = soknadMetadataRepository.save(opprettSoknadMetadata()).soknadId
        soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val staticSubjectHandlerImpl = StaticSubjectHandlerImpl().apply { setUser(soknad.eierPersonId) }
        SubjectHandlerUtils.setNewSubjectHandlerImpl(staticSubjectHandlerImpl)
    }
}
