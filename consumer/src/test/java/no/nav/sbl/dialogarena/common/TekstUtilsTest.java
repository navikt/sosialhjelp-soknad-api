package no.nav.sbl.dialogarena.common;

import static no.nav.sbl.dialogarena.common.TekstUtils.fjernSpesialtegn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

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
}
