package no.nav.sosialhjelp.soknad

import io.mockk.impl.annotations.InjectMockKs
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import no.nav.sosialhjelp.soknad.tekster.TeksterConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-redis", "test"])
class ApplicationContextTest {

    @InjectMockKs
    private var teksterConfig = TeksterConfig(false)

    @Test
    internal fun skalStarte() {
    }

    @Test
    fun skalReturnereRettAntallBundles() {
        val source: NavMessageSource = teksterConfig.navMessageSource()
        val basenames = source.getBasenames()
        assertThat(basenames).hasSize(1)
    }
}
