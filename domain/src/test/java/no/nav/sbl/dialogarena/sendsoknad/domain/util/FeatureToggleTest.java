package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import org.junit.After;
import org.junit.Test;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.Toggle.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeatureToggleTest {

    @After
    public void teardown() {
        clearProperty(RESSURS_ALTERNATIVREPRESENTASJON.getPropertyNavn());
        clearProperty(RESSURS_FULLOPPSUMERING.getPropertyNavn());
    }

    @Test
    public void erFeatureAktivReturnererTrueHvisSystemPropertyErSattTilTrue() {
        setProperty(RESSURS_FULLOPPSUMERING.getPropertyNavn(), "true");

        final boolean erAktiv = erFeatureAktiv(RESSURS_FULLOPPSUMERING);

        assertThat(erAktiv, is(true));
    }

    @Test
    public void erFeatureAktivReturnererFalseHvisSystemPropertyErSattTilFalse() {
        setProperty(RESSURS_ALTERNATIVREPRESENTASJON.getPropertyNavn(), "false");

        final boolean erAktiv = erFeatureAktiv(RESSURS_ALTERNATIVREPRESENTASJON);

        assertThat(erAktiv, is(false));
    }

    @Test
    public void erFeatureAktivReturnererFalseHvisPropertyIkkeErSatt() {
        teardown();

        final boolean erAktiv = erFeatureAktiv(RESSURS_FULLOPPSUMERING);

        assertThat(erAktiv, is(false));
    }
}
