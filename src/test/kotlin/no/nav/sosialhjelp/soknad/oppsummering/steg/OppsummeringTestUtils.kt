package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import org.assertj.core.api.Assertions.assertThat

internal object OppsummeringTestUtils {
    fun validateFeltMedSvar(felt: Felt, type: Type?, svarType: SvarType?, svarValue: String?) {
        assertThat(felt.type).isEqualTo(type)
        assertThat(felt.svar?.type).isEqualTo(svarType)
        assertThat(felt.svar?.value).isEqualTo(svarValue)
    }
}
