package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.MockOAuth2Initializer
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = [MockOAuth2Initializer::class])
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractJobTest {
    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    @Autowired
    protected lateinit var metadataRepository: SoknadMetadataRepository
}
