package no.nav.sbl.dialogarena.common;

import no.nav.sbl.dialogarena.konto.Formatering;
import org.junit.Test;

import static no.nav.sbl.dialogarena.common.TekstUtils.fjernSpesialtegn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tester felles util-metoder
 */
public class TekstUtilsTest {

    @Test
    public void skalFjerneSpesialTegn() {
        assertThat(fjernSpesialtegn("1234 - 56 - 78910."), is("12345678910"));
    }

    @Test
    public void fjerneSpesialtegnFraNullErNull() {
        assertThat(fjernSpesialtegn(null), is(nullValue()));
    }

    @Test
    public void skalFormatereKontonummer() {
        assertThat(Formatering.formaterNorskKontonummer("12345678910"), is("1234 56 78910"));
        assertThat(Formatering.formaterNorskKontonummer("  1234.56.78910   "), is("1234 56 78910"));
    }

    @Test
    public void formateringAvKontonummerHarIngenValideringMenSetterKunInnSpacePaaRettSted() {
        assertThat(Formatering.formaterNorskKontonummer("1234. 56. 78910123"), is("1234 56 78910123"));
    }

    @Test
    public void formaterNullReturnererNull() {
        assertThat(Formatering.formaterNorskKontonummer(null), is(nullValue()));
    }
}
