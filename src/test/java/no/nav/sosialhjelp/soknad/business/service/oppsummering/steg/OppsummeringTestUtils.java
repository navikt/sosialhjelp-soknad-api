package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import static org.assertj.core.api.Assertions.assertThat;

public final class OppsummeringTestUtils {

    public static void validateFeltMedSvar(Felt felt, Type type, SvarType svarType, String svarValue) {
        assertThat(felt.getType()).isEqualTo(type);
        assertThat(felt.getSvar().getType()).isEqualTo(svarType);
        assertThat(felt.getSvar().getValue()).isEqualTo(svarValue);
    }

    private OppsummeringTestUtils() {
        //no-op
    }
}
