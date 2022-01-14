package no.nav.sosialhjelp.soknad

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-redis", "test"])
class ApplicationContextTest {

    @Test
    internal fun skalStarte() {
    }
}
