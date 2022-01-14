package no.nav.sosialhjelp.soknad

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import no.nav.sosialhjelp.soknad.web.config.ContentConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-redis", "test"])
class ApplicationContextTest {

    @InjectMocks
    var contentConfig: ContentConfig? = null

    @Test
    internal fun skalStarte() {
    }

    @Test
    fun skalReturnereRettAntallBundles() {
        val source: NavMessageSource = contentConfig!!.navMessageSource()
        val basenames = source.basenames
        assertThat(basenames).hasSize(1)
    }
}
