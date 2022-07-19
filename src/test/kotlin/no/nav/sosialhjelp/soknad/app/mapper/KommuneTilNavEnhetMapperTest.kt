package no.nav.sosialhjelp.soknad.app.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KommuneTilNavEnhetMapperTest {

    @Test
    fun testGetOrganisasjonsnummerOfNull() {
        val result = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(null)
        assertThat(result).isNull()
    }
}
