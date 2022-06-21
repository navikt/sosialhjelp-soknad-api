package no.nav.sosialhjelp.soknad.innsending

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SenderUtilsTest {

    private var originalBehandlingsId = "behandlingsId"

    @Test
    fun createPrefixedBehandlingsId_shouldBePrefixedWithEnvironmentName() {
        mockkObject(MiljoUtils)

        every { MiljoUtils.environmentName } returns "q0"
        var prefixedBehandlingsId = createPrefixedBehandlingsId(originalBehandlingsId)
        assertThat(prefixedBehandlingsId).isEqualTo("q0-$originalBehandlingsId")

        every { MiljoUtils.environmentName } returns "q1"
        prefixedBehandlingsId = createPrefixedBehandlingsId(originalBehandlingsId)
        assertThat(prefixedBehandlingsId).isEqualTo("q1-$originalBehandlingsId")

        unmockkObject(MiljoUtils)
    }
}
