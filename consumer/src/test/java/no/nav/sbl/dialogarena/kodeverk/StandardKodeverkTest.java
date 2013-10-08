package no.nav.sbl.dialogarena.kodeverk;

import no.nav.modig.core.exception.SystemException;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkResponse;
import org.junit.Assert;
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
    public void kanHentePoststedBasertPaaPostnummer() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        XMLHentKodeverkResponse response = postnummerKodeverkResponse();
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(response);

        String poststed = kodeverk.getPoststed("0565");
        Assert.assertEquals("Oslo", poststed);
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

    private XMLHentKodeverkResponse postnummerKodeverkResponse() {
        XMLKode kode = new XMLKode().withNavn("0565").withTerm(new XMLTerm().withNavn("Oslo"));
        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Kommuner").withKode(kode));
    }

    private static XMLHentKodeverkResponse landkodeKodeverkResponse() {
        XMLKode norge = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge"));
        XMLKode sverige = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige"));
        XMLKode albania = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania"));
        XMLKode danmark = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark"));

        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge, sverige, albania, danmark));
    }
}