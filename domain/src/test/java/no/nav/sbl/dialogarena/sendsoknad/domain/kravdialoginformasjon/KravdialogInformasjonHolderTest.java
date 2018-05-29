package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.modig.core.exception.ApplicationException;
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
    public void alleForeldrepengeskjema() {
        KravdialogInformasjonHolder kravdialogInformasjonHolder = new KravdialogInformasjonHolder();
        KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(ForeldrepengerInformasjon.ENDRING_OVERFORING.get(0));
        assertThat(konfigurasjon.getSkjemanummer(), containsInAnyOrder("NAV 14-05.06", "NAV 14-05.07", "NAV 14-05.08", "NAV 14-05.09", "NAV 14-05.10"));
    }
    
    @Test
    public void skalHenteAlleSkjemanummerSomFinnes() {
        KravdialogInformasjonHolder kravdialogInformasjonHolder = new KravdialogInformasjonHolder();
        List<String> skjemanummer = kravdialogInformasjonHolder.hentAlleSkjemanumre();
        assertThat(skjemanummer, is(not(empty())));
    }

}