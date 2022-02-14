package no.nav.sosialhjelp.soknad.common.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KommuneTilNavEnhetMapperTest {

    @Test
    fun testGetOrganisasjonsnummerOfNull() {
        val result = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(null)
        assertThat(result).isNull()
    }
}
