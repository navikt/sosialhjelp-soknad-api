package no.nav.sosialhjelp.soknad.v2.register

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.arbeid.AaregClient
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OrganisasjonDto
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonClient
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractRegisterDataTest {
    @Autowired
    protected lateinit var soknadRepository: SoknadRepository
    protected val soknad = opprettSoknad(id = UUID.randomUUID())

    @BeforeEach
    fun setup() {
        soknadRepository.save(soknad)

        val staticSubjectHandlerImpl = StaticSubjectHandlerImpl().apply { setUser(soknad.eierPersonId) }
        SubjectHandlerUtils.setNewSubjectHandlerImpl(staticSubjectHandlerImpl)
    }

    @MockkBean
    protected lateinit var aaregClient: AaregClient

    @MockkBean
    protected lateinit var organisasjonClient: OrganisasjonClient

    protected fun createAnswerForAaregClient(
        answer: List<ArbeidsforholdDto> = defaultResponseFromAaregClient(soknad.eierPersonId),
    ): List<ArbeidsforholdDto> {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(soknad.eierPersonId) } returns answer
        return answer
    }

    protected fun createAnswerForOrganisasjonClient(
        arbeidsforhold: List<ArbeidsforholdDto>,
    ): List<OrganisasjonNoekkelinfoDto> {
        return arbeidsforhold
            .map { (it.arbeidsgiver as OrganisasjonDto).organisasjonsnummer }
            .map {
                val answer = defaultResponseFromOrganisasjonClient(it!!)
                every { organisasjonClient.hentOrganisasjonNoekkelinfo(it) } returns answer
                answer
            }
    }
}
