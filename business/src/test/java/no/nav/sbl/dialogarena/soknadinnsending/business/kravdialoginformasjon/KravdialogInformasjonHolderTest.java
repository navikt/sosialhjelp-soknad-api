package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class KravdialogInformasjonHolderTest {

    @Test
    public void skalHenteKonfigBasertPaaSkjemanummer() {
        KravdialogInformasjonHolder kravdialogInformasjonHolder = new KravdialogInformasjonHolder();
        KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon("NAV 11-13.05");
        assertThat(konfigurasjon.getSkjemanummer(), contains("NAV 11-13.05"));
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