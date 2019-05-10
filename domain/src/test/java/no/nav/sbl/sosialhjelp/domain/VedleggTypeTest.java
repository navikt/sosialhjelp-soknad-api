package no.nav.sbl.sosialhjelp.domain;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class VedleggTypeTest {

    private static final String TYPE = "bostotte|kontooversikt";
    private static final String TYPE2 = "annetboutgift|brukskonto";

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgTilleggsinfoErLike() {
        VedleggType vedleggType = new VedleggType(TYPE);
        VedleggType likVedleggType = new VedleggType(TYPE);

        assertThat(vedleggType.equals(likVedleggType), is(true));
    }

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgTilleggsinfoHarSammeHashVerdi() {
        List<VedleggType> vedleggTypeList = new ArrayList<>(Arrays.asList(new VedleggType(TYPE), new VedleggType(TYPE)));

        Set<VedleggType> vedleggTyper = new HashSet<>();
        vedleggTypeList.removeIf(type -> !vedleggTyper.add(type));

        assertThat(vedleggTyper.size(), is(1));
        assertThat(vedleggTypeList.size(), is(1));
    }

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgUlikTilleggsinfoErUlike() {
        VedleggType vedleggType = new  VedleggType(TYPE);
        VedleggType likVedleggType = new VedleggType(TYPE2);

        assertThat(vedleggType.equals(likVedleggType), is(false));
    }

    @Test
    public void vedleggTypeObjekterMedUlikTypeOgSammeTilleggsinfoErUlike() {
        VedleggType vedleggType = new  VedleggType(TYPE);
        VedleggType likVedleggType = new VedleggType(TYPE2);

        assertThat(vedleggType.equals(likVedleggType), is(false));
    }
}