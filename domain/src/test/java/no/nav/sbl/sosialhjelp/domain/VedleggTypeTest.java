package no.nav.sbl.sosialhjelp.domain;

import org.junit.Test;

import static no.nav.sbl.sosialhjelp.domain.VedleggType.mapSammensattVedleggTypeTilVedleggType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class VedleggTypeTest {

    private static final String TYPE = "bostotte";
    private static final String TILLEGGSINFO = "annetboutgift";
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
}