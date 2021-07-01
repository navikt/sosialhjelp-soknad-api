package no.nav.sosialhjelp.soknad.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class VedleggTypeTest {

    private static final String TYPE = "bostotte|kontooversikt";
    private static final String TYPE2 = "annetboutgift|brukskonto";

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgTilleggsinfoErLike() {
        VedleggType vedleggType = new VedleggType(TYPE);
        VedleggType likVedleggType = new VedleggType(TYPE);

        assertThat(vedleggType.equals(likVedleggType)).isTrue();
    }

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgTilleggsinfoHarSammeHashVerdi() {
        List<VedleggType> vedleggTypeList = new ArrayList<>(Arrays.asList(new VedleggType(TYPE), new VedleggType(TYPE)));

        Set<VedleggType> vedleggTyper = new HashSet<>();
        vedleggTypeList.removeIf(type -> !vedleggTyper.add(type));

        assertThat(vedleggTyper).hasSize(1);
        assertThat(vedleggTypeList).hasSize(1);
    }

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgUlikTilleggsinfoErUlike() {
        VedleggType vedleggType = new VedleggType(TYPE);
        VedleggType likVedleggType = new VedleggType(TYPE2);

        assertThat(vedleggType.equals(likVedleggType)).isFalse();
    }

    @Test
    public void vedleggTypeObjekterMedUlikTypeOgSammeTilleggsinfoErUlike() {
        VedleggType vedleggType = new VedleggType(TYPE);
        VedleggType likVedleggType = new VedleggType(TYPE2);

        assertThat(vedleggType.equals(likVedleggType)).isFalse();
    }
}