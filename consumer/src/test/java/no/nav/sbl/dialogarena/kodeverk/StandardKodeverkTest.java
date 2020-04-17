package no.nav.sbl.dialogarena.kodeverk;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLPeriode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkResponse;
import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardKodeverkTest {

    @Mock
    private KodeverkPortType ws;

    private Kodeverk kodeverk;

    private final File dumpDir = Paths.get("target/kodeverkdump" + randomNumeric(10)).toAbsolutePath().toFile();

    @Before
    public void wireUpKodeverk() {
        if (!dumpDir.exists()){
            assertTrue(dumpDir.mkdirs());
        }
        kodeverk = new StandardKodeverk(ws, Locale.getDefault(), Optional.of(dumpDir));
    }

    @Test
    public void kanHentePoststedBasertPaaPostnummer() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        XMLHentKodeverkResponse response = postnummerKodeverkResponse();
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(response);

        String poststed = kodeverk.getPoststed("0565");
        assertEquals("Oslo", poststed);
    }

    @Test
    public void skalKunneSlaaOppTermBasertPaaKodeOgOmvendt() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());
        assertThat(kodeverk.getLand("NOR"), is("Norge"));
    }

    @Test
    public void skalFiltrereVekkUgyldigePerioder() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponseInkludertUgyldigePerioder());
        assertThat(kodeverk.getLand("NOR"), nullValue());
        assertThat(kodeverk.getLand("SWE"), is("Sverige"));
        assertThat(kodeverk.getLand("ALB"), nullValue());
    }

    @Test(expected = SosialhjelpSoknadApiException.class)
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
    }

    private XMLHentKodeverkResponse postnummerKodeverkResponse() {
        XMLKode kode = new XMLKode().withNavn("0565").withTerm(new XMLTerm().withNavn("Oslo")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Kommuner").withKode(kode));
    }

    private static XMLHentKodeverkResponse landkodeKodeverkResponse() {
        XMLKode norge = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode aaland = new XMLKode().withNavn("ALA").withTerm(new XMLTerm().withNavn("Åland")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode sverige = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode oesttemor = new XMLKode().withNavn("OST").withTerm(new XMLTerm().withNavn("Øst-Temor")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode albania = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode danmark = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));

        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge, aaland, oesttemor, sverige, albania, danmark));
    }

    private static XMLHentKodeverkResponse landkodeKodeverkResponseInkludertUgyldigePerioder() {
        XMLKode norge_utlopt = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(2)).withTom(DateMidnight.now().minusDays(1)));
        XMLKode sverige_gyldig = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode albania_fremtidig = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().plusDays(1)).withTom(DateMidnight.now().plusDays(2)));
        XMLKode danmark_ingen_slutt = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark")).withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(null));

        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge_utlopt, sverige_gyldig, albania_fremtidig, danmark_ingen_slutt));
    }
}