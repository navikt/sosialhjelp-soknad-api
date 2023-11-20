package no.nav.sosialhjelp.soknad.nymodell.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["mock-alt", "no-redis", "test"])
abstract class SoknadApiIntergrationTest {

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    @Autowired
    protected lateinit var oldSoknadService: OldSoknadService

    @Autowired
    protected lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @MockkBean
    protected lateinit var systemdataUpdater: SystemdataUpdater

    @MockkBean
    protected lateinit var unleash: Unleash

    @BeforeEach
    fun setup() {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
        every { systemdataUpdater.update(any()) } just runs
        every { unleash.isEnabled(any()) } returns false
    }

    protected fun<T> doRequest(url: String, method: HttpMethod, entity: HttpEntity<T>, clazz: Class<T>): ResponseEntity<T> {
        return restTemplate.exchange(url, method, entity, clazz)
    }
}
