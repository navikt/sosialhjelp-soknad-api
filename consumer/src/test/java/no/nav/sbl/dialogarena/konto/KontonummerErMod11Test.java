package no.nav.sbl.dialogarena.konto;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class KontonummerErMod11Test {

    private static final String GYLDIG_KONTONUMMER_REST_NULL        = "12445678900";
    private static final String GYLDIG_KONTONUMMER_REST_ULIK_NULL   = "12345678903";
    private static final String UGYLDIG_KONTONUMMER_1               = "12445678903";


    @Test
    public void sjekkAvGyldigKontonummer() {
        KontonummerErMod11 erMod11 = new KontonummerErMod11();

        assertThat(erMod11.evaluate(GYLDIG_KONTONUMMER_REST_NULL), is(true));
        assertThat(erMod11.evaluate(GYLDIG_KONTONUMMER_REST_ULIK_NULL), is(true));
        assertThat(erMod11.evaluate(UGYLDIG_KONTONUMMER_1), is(false));
    }

}
