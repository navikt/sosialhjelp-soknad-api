package no.nav.sbl.dialogarena.websoknad.servlet;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;

import java.util.List;

import no.nav.modig.core.exception.ApplicationException;

import org.junit.Test;

public class NokkelHenterTest {
    @Test
    public void skalHenteEnNokkel() {
        List<String> nokleListe = NokkelHenter.hentNokler("testfil1");
        assertThat(nokleListe.size(), is(1));
        assertThat("hei", isIn(nokleListe));
    }

    @Test
    public void skalHenteFlereNokler() {
        List<String> nokleListe = NokkelHenter.hentNokler("testfil2");
        assertThat(nokleListe.size(), is(2));
        assertThat("hallo", isIn(nokleListe));
        assertThat("hadet", isIn(nokleListe));
    }

    @Test(expected = ApplicationException.class)
    public void skalSendeInnEnFilSomIkkeEksisterer() {
        List<String> nokleListe = NokkelHenter.hentNokler("test");
        assertThat(nokleListe.size(), is(0));
    }
}