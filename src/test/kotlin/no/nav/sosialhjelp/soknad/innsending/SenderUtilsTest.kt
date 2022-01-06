package no.nav.sosialhjelp.soknad.innsending

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class SenderUtilsTest {

    var originalBehandlingsId = "behandlingsId"

    @AfterEach
    fun tearDown() {
        System.clearProperty("environment.name")
    }

    @Test
    fun createPrefixedBehandlingsId_inProd_shouldNotBePrefixed() {
        System.clearProperty("environment.name")
        var prefixedBehandlingsId = SenderUtils.createPrefixedBehandlingsIdInNonProd(originalBehandlingsId)
        assertThat(prefixedBehandlingsId).isEqualTo(originalBehandlingsId)

        System.setProperty("environment.name", "p")
        prefixedBehandlingsId = SenderUtils.createPrefixedBehandlingsIdInNonProd(originalBehandlingsId)
        assertThat(prefixedBehandlingsId).isEqualTo(originalBehandlingsId)

        System.setProperty("environment.name", "ukjent")
        prefixedBehandlingsId = SenderUtils.createPrefixedBehandlingsIdInNonProd(originalBehandlingsId)
        assertThat(prefixedBehandlingsId).isEqualTo(originalBehandlingsId)
    }

    @Test
    fun createPrefixedBehandlingsId_inNonProd_shouldBePrefixedWithEnvironmentName() {
        System.setProperty("environment.name", "q0")
        var prefixedBehandlingsId = SenderUtils.createPrefixedBehandlingsIdInNonProd(originalBehandlingsId)
        assertThat(prefixedBehandlingsId).isEqualTo("q0-$originalBehandlingsId")

        System.setProperty("environment.name", "q1")
        prefixedBehandlingsId = SenderUtils.createPrefixedBehandlingsIdInNonProd(originalBehandlingsId)
        assertThat(prefixedBehandlingsId).isEqualTo("q1-$originalBehandlingsId")
    }
}
