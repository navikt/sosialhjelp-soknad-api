package no.nav.sbl.sosialhjelp.domain;

import org.junit.Test;

import static no.nav.sbl.sosialhjelp.domain.VedleggType.mapSammensattVedleggTypeTilVedleggType;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class VedleggTypeTest {

    private static final String TYPE = "bostotte";
    private static final String TYPE2 = "kontooversikt";
    private static final String TILLEGGSINFO = "annetboutgift";
    private static final String TILLEGGSINFO2 = "brukskonto";
    private static final String GYLDIG_SAMMENSATT_VEDLEGGTYPE = "bostotte|annetboutgift";

    @Test
    public void mapSammensattVedleggTypeTilVedleggTypeLagerVedleggTypeMedRiktigInfo() {
        VedleggType vedleggType = mapSammensattVedleggTypeTilVedleggType(GYLDIG_SAMMENSATT_VEDLEGGTYPE);

        assertThat(vedleggType, notNullValue());
        assertThat(vedleggType.getType(), is(TYPE));
        assertThat(vedleggType.getTilleggsinfo(), is(TILLEGGSINFO));
    }

    @Test
    public void mapSammensattVedleggTypeTilVedleggTypeReturnererNullHvisInputErNull() {
        VedleggType vedleggType = mapSammensattVedleggTypeTilVedleggType(null);

        assertThat(vedleggType, nullValue());
    }

    @Test
    public void mapSammensattVedleggTypeTilVedleggTypeReturnererNullHvisInputHarFlerePipes() {
        VedleggType vedleggType = mapSammensattVedleggTypeTilVedleggType("test|test2|test3");

        assertThat(vedleggType, nullValue());
    }

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgTilleggsinfoErLike() {
        VedleggType vedleggType = new VedleggType(TYPE, TILLEGGSINFO);
        VedleggType likVedleggType = new VedleggType(TYPE, TILLEGGSINFO);

        assertThat(vedleggType.equals(likVedleggType), is(true));
    }

    @Test
    public void vedleggTypeObjekterMedSammeTypeOgUlikTilleggsinfoErUlike() {
        VedleggType vedleggType = new VedleggType(TYPE, TILLEGGSINFO);
        VedleggType likVedleggType = new VedleggType(TYPE, TILLEGGSINFO2);

        assertThat(vedleggType.equals(likVedleggType), is(false));
    }

    @Test
    public void vedleggTypeObjekterMedUlikTypeOgSammeTilleggsinfoErUlike() {
        VedleggType vedleggType = new VedleggType(TYPE, TILLEGGSINFO);
        VedleggType likVedleggType = new VedleggType(TYPE2, TILLEGGSINFO);

        assertThat(vedleggType.equals(likVedleggType), is(false));
    }
}