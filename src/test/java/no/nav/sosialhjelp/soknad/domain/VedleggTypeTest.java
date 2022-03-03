package no.nav.sosialhjelp.soknad.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VedleggTypeTest {

    private static final String TYPE = "bostotte|kontooversikt";
    private static final String TYPE2 = "annetboutgift|brukskonto";

    @Test
    void vedleggTypeObjekterMedSammeTypeOgTilleggsinfoErLike() {
        OpplastetVedleggType vedleggType = new OpplastetVedleggType(TYPE);
        OpplastetVedleggType likVedleggType = new OpplastetVedleggType(TYPE);

        assertThat(vedleggType.equals(likVedleggType)).isTrue();
    }

    @Test
    void vedleggTypeObjekterMedSammeTypeOgTilleggsinfoHarSammeHashVerdi() {
        List<OpplastetVedleggType> vedleggTypeList = new ArrayList<>(Arrays.asList(new OpplastetVedleggType(TYPE), new OpplastetVedleggType(TYPE)));

        Set<OpplastetVedleggType> vedleggTyper = new HashSet<>();
        vedleggTypeList.removeIf(type -> !vedleggTyper.add(type));

        assertThat(vedleggTyper).hasSize(1);
        assertThat(vedleggTypeList).hasSize(1);
    }

    @Test
    void vedleggTypeObjekterMedSammeTypeOgUlikTilleggsinfoErUlike() {
        OpplastetVedleggType vedleggType = new OpplastetVedleggType(TYPE);
        OpplastetVedleggType likVedleggType = new OpplastetVedleggType(TYPE2);

        assertThat(vedleggType.equals(likVedleggType)).isFalse();
    }

    @Test
    void vedleggTypeObjekterMedUlikTypeOgSammeTilleggsinfoErUlike() {
        OpplastetVedleggType vedleggType = new OpplastetVedleggType(TYPE);
        OpplastetVedleggType likVedleggType = new OpplastetVedleggType(TYPE2);

        assertThat(vedleggType.equals(likVedleggType)).isFalse();
    }
}