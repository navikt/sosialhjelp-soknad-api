package no.nav.sbl.dialogarena.konto;

import org.apache.commons.collections15.Predicate;
import org.junit.Test;

import static no.nav.modig.lang.collections.PredicateUtils.both;
import static no.nav.sbl.dialogarena.konto.GyldigNorskKontonummer.ELLEVE_SIFFER;
import static no.nav.sbl.dialogarena.konto.GyldigNorskKontonummer.OPPFYLLER_MOD11;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GyldigNorskKontonummerTest {

    private final Predicate<String> gyldigKontonummer = both(ELLEVE_SIFFER).and(OPPFYLLER_MOD11);

    @Test
    public void kontonummerMaaVaere11Tall() {
        assertThat(gyldigKontonummer.evaluate("05401105575"), is(true));
        assertThat(gyldigKontonummer.evaluate("53681042373"), is(true));

        assertThat(gyldigKontonummer.evaluate("53681042"), is(false));
        assertThat(gyldigKontonummer.evaluate("536810422342"), is(false));
        assertThat(gyldigKontonummer.evaluate("ab681042373"), is(false));
        assertThat(gyldigKontonummer.evaluate("ab681042373"), is(false));
        assertThat(gyldigKontonummer.evaluate("ab681042373234"), is(false));
    }

    @Test
    public void kontonummerKanHaSeparatorTegn() {
        assertThat(gyldigKontonummer.evaluate("5368 10 42373"), is(true));
        assertThat(gyldigKontonummer.evaluate("5368.10.42373"), is(true));
        assertThat(gyldigKontonummer.evaluate("5368,10,42373"), is(true));
        assertThat(gyldigKontonummer.evaluate("5368-10-42373"), is(true));

        assertThat(gyldigKontonummer.evaluate("5368a10a42373"), is(false));
    }

    @Test
    public void kontonummerMaaVaereEtGyldigKontonummer() {
        assertThat(gyldigKontonummer.evaluate("05401105571"), is(false));
        assertThat(gyldigKontonummer.evaluate("53681042370"), is(false));
    }

}
