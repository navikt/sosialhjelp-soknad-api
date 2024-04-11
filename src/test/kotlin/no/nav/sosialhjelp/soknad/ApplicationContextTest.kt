package no.nav.sosialhjelp.soknad

import io.mockk.impl.annotations.InjectMockKs
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import no.nav.sosialhjelp.soknad.tekster.TeksterConfig
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApplicationContextTest : AbstractIntegrationTest() {
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
