package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.ApplicationException;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class KravdialogInformasjonHolderTest {

    @Test
    public void skalHenteKonfigBasertPaaSkjemanummer() {
        KravdialogInformasjonHolder kravdialogInformasjonHolder = new KravdialogInformasjonHolder();
        KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon("NAV 35-18.01");
        assertThat(konfigurasjon.getSkjemanummer(), contains("NAV 35-18.01"));
    }

    @Test(expected = ApplicationException.class)
    public void skalKasteFeilHvisSkjemanummerIkkeFinnes() {
        new KravdialogInformasjonHolder().hentKonfigurasjon("skjemaSomIkkeFinnes");
    }
    
    @Test
    public void skalHenteAlleSkjemanummerSomFinnes() {
        KravdialogInformasjonHolder kravdialogInformasjonHolder = new KravdialogInformasjonHolder();
        List<String> skjemanummer = kravdialogInformasjonHolder.hentAlleSkjemanumre();
        assertThat(skjemanummer, is(not(empty())));
    }

}