package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SenderUtilsTest {

    var originalBehandlingsId = "behandlingsId"

    @Test
    fun createPrefixedBehandlingsId_shouldBePrefixedWithEnvironmentName() {
        var prefixedBehandlingsId = createPrefixedBehandlingsId(originalBehandlingsId, "q0")
        assertThat(prefixedBehandlingsId).isEqualTo("q0-$originalBehandlingsId")

        prefixedBehandlingsId = createPrefixedBehandlingsId(originalBehandlingsId, "q1")
        assertThat(prefixedBehandlingsId).isEqualTo("q1-$originalBehandlingsId")
    }
}
