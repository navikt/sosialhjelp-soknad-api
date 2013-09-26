package no.nav.sbl.dialogarena.kodeverk;

import no.nav.modig.core.exception.SystemException;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static no.nav.modig.core.test.FilesAndDirs.BUILD_OUTPUT;
import static no.nav.modig.lang.option.Optional.optional;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardKodeverkTest {

    @Mock
    private KodeverkPortType ws;

    private Kodeverk kodeverk;

    private final File dumpDir = new File(BUILD_OUTPUT, "kodeverkdump/" + randomNumeric(10));


    @Before
    public void wireUpKodeverk() {
        kodeverk = new StandardKodeverk(ws, Locale.getDefault(), optional(dumpDir));
    }



    @Test
     public void landkodeKodeverkSkalSorteres() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());

        List<String> alleLandkoder = kodeverk.getAlleLandkoder();
        assertThat(alleLandkoder, contains("ALB", "DNK", "SWE"));
    }

    @Test
    public void norgeSkalIkkeFolgeMedRestenAvLandene() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());

        List<String> alleLandkoder = kodeverk.getAlleLandkoder();
        assertThat(alleLandkoder, not(contains("NOR")));
    }

    @Test
    public void retningsnummerKodeverkSkalSorteres() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(retningsnummerKodeverkResponse());

        List<String> alleTelefonLandkoder = kodeverk.getAlleTelefonnummerLandkoder();
        assertThat(alleTelefonLandkoder, contains("+213", "+1", "+45", "+47", "+1456", "+46"));
    }

    @Test
    public void skalKunneSlaaOppTermBasertPaaKodeOgOmvendt() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());

        assertThat(kodeverk.getLand("NOR"), is("Norge"));
        assertThat(kodeverk.getLandkode("Norge"), is("NOR"));
    }

    @Test(expected = SystemException.class)
    public void ugyldigKodeverknavnGirSystemException() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenThrow(new HentKodeverkHentKodeverkKodeverkIkkeFunnet());
        kodeverk.lastInnNyeKodeverk();
    }

    @Test
    public void dumperInnlastetKodeverkTilFileOgBrukerDenneVedRestartDaKodeverkErNede() throws Exception {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());
        kodeverk.lastInnNyeKodeverk();

        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenThrow(new RuntimeException("Kodeverk er nede"));
        kodeverk.lastInnNyeKodeverk();
        wireUpKodeverk();
        kodeverk.lastInnNyeKodeverk();
    }

    private static XMLHentKodeverkResponse landkodeKodeverkResponse() {
        XMLKode norge = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge"));
        XMLKode sverige = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige"));
        XMLKode albania = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania"));
        XMLKode danmark = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark"));

        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge, sverige, albania, danmark));
    }

    private static XMLHentKodeverkResponse retningsnummerKodeverkResponse() {
        XMLKode norge = new XMLKode().withNavn("+47").withTerm(new XMLTerm().withNavn("Norge"));
        XMLKode sverige = new XMLKode().withNavn("+46").withTerm(new XMLTerm().withNavn("Sverige"));
        XMLKode albania = new XMLKode().withNavn("+213").withTerm(new XMLTerm().withNavn("Albania"));
        XMLKode uscanada = new XMLKode().withNavn("+1").withTerm(new XMLTerm().withNavn("Canada, USA"));
        XMLKode saotome = new XMLKode().withNavn("+1456").withTerm(new XMLTerm().withNavn("Sao Tome og Principe"));
        XMLKode danmark = new XMLKode().withNavn("+45").withTerm(new XMLTerm().withNavn("Danmark"));

        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Retningsnumre").withKode(norge, sverige, albania, uscanada, saotome, danmark));
    }
}
