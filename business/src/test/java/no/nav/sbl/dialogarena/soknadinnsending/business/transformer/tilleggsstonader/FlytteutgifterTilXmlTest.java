package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;


import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Anbud;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Flytteutgifter;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.FlytteutgifterTilXml;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier.FlytterSelv.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FlytteutgifterTilXmlTest {

    public static final String GATEADRESSE = "NAVveien 3";
    public static final String POSTNUMMER = "1234";
    public static final String LAND = "Norge";
    public static final String UTENLANDSK_ADRESSE = "Svenskegata 3, Gøteborg";
    public static final String AVSTAND_KM = "500";
    public static final String HENGERLEIE_KR = "100";
    public static final String NAVN_FLYTTEBYRAA_1 = "Petters flytting";
    public static final String BELOP_FLYTTEBYRAA_1 = "5000";
    public static final String NAVN_FLYTTEBYRAA_2 = "Siljes flytting";
    public static final String BELOP_FLYTTEBYRAA_2 = "9000";
    private static final String BOM_KR = "200";
    private static final String PARKERING_KR = "300";
    private static final String FERGE_KR = "400";
    private static final String ANNET_KR = "500";
    private Flytteutgifter flytteutgifter;
    @Mock
    MessageSource messageSource;
    @InjectMocks
    private FlytteutgifterTilXml flytteutgifterTilXml;
    private WebSoknad webSoknad;
    private Faktum aktivitet;
    private Faktum tiltredelsesdato;
    private Faktum flyttedato;
    private Faktum adresse;
    private Faktum avstand;
    private Faktum hengerleie;
    private Faktum bom;
    private Faktum parkering;
    private Faktum ferge;
    private Faktum annet;
    private Faktum forsteAnbudNavn;
    private Faktum andreAnbudNavn;
    private Faktum forsteAnbudBelop;
    private Faktum andreAnbudBelop;
    private Faktum valgtForste;
    private Faktum valgtAndre;
    private Faktum nyeaddresse;
    private Faktum personalia;
    private Faktum flyttingDekket;

    @Before
    public void beforeEach() {
        personalia = new Faktum()
        .medKey("personalia")
        .medProperty("gjeldendeAdresse", "gjeldende adresse");
        nyeaddresse = new Faktum().medKey("flytting.tidligere.riktigadresse").medValue("false");
        aktivitet = new Faktum()
                .medKey("flytting.hvorforflytte")
                .medValue("aktivitet");
        tiltredelsesdato = new Faktum()
                .medKey("flytting.nyjobb.startdato")
                .medValue("2015-07-22");
        flyttedato = new Faktum()
                .medKey("flytting.nyjobb.flyttedato")
                .medValue("2015-10-22");
        adresse = new Faktum()
                .medKey("flytting.nyadresse")
                .medProperty("land", LAND)
                .medProperty("adresse", GATEADRESSE)
                .medProperty("postnr", POSTNUMMER)
                .medProperty("utenlandskadresse", UTENLANDSK_ADRESSE);
        avstand = new Faktum()
                .medKey("flytting.flytteselv.hvorlangt")
                .medValue(AVSTAND_KM);
        hengerleie = new Faktum()
                .medKey("flytting.flytteselv.andreutgifter.hengerleie")
                .medValue(HENGERLEIE_KR);
        bom = new Faktum()
                .medKey("flytting.flytteselv.andreutgifter.bom")
                .medValue(BOM_KR);
        parkering = new Faktum()
                .medKey("flytting.flytteselv.andreutgifter.parkering")
                .medValue(PARKERING_KR);
        ferge = new Faktum()
                .medKey("flytting.flytteselv.andreutgifter.ferge")
                .medValue(FERGE_KR);
        annet = new Faktum()
                .medKey("flytting.flytteselv.andreutgifter.annet")
                .medValue(ANNET_KR);
        forsteAnbudNavn = new Faktum()
                .medKey("flytting.flyttebyraa.forste.navn")
                .medValue(NAVN_FLYTTEBYRAA_1);
        forsteAnbudBelop = new Faktum()
                .medKey("flytting.flyttebyraa.forste.belop")
                .medValue(BELOP_FLYTTEBYRAA_1);
        andreAnbudNavn = new Faktum()
                .medKey("flytting.flyttebyraa.andre.navn")
                .medValue(NAVN_FLYTTEBYRAA_2);
        andreAnbudBelop = new Faktum()
                .medKey("flytting.flyttebyraa.andre.belop")
                .medValue(BELOP_FLYTTEBYRAA_2);
        valgtForste = new Faktum()
                .medKey("flytting.flyttebyraa.velgforste")
                .medValue("true");
        valgtAndre = new Faktum()
                .medKey("flytting.flyttebyraa.velgandre")
                .medValue("true");
        flyttingDekket = new Faktum()
                .medKey("flytting.dekket")
                .medValue("true");



    }


    @Test
    public void settFlyttingPgaAktivitet() {
        webSoknad = new WebSoknad().medFaktum(aktivitet);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.isFlyttingPgaAktivitet().booleanValue()).isEqualTo(true);
    }

    @Test
    public void settFlyttingPgNyStilling() {
        aktivitet.setValue("nyjobb");
        webSoknad = new WebSoknad().medFaktum(aktivitet);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.isFlyttingPgaNyStilling().booleanValue()).isEqualTo(true);
    }

    @Test
    public void settTiltredelsesdato() {
        webSoknad = new WebSoknad().medFaktum(tiltredelsesdato);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getTiltredelsesdato().toString()).isEqualTo("2015-07-22T00:00:00.000+02:00");
    }

    @Test
    public void settFlyttedato() {
        webSoknad = new WebSoknad().medFaktum(flyttedato);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getFlyttedato().toString()).isEqualTo("2015-10-22T00:00:00.000+02:00");
    }

    @Test
    public void settTilFlyttingsadresseINorge() {
        webSoknad = new WebSoknad().medFaktum(adresse).medFaktum(nyeaddresse);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getTilflyttingsadresse()).isEqualTo(GATEADRESSE + ", " + POSTNUMMER);
    }

    @Test
    public void settTilFlyttingsadresseUtland() {
        webSoknad = new WebSoknad()
                .medFaktum(adresse.medProperty("land", "Sverige"))
                .medFaktum(nyeaddresse);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getTilflyttingsadresse()).isEqualTo(UTENLANDSK_ADRESSE + ", Sverige");
    }

    @Test
    public void settTilFlyttingsadresseFraPersonalia() {
        webSoknad = new WebSoknad().medFaktum(adresse).medFaktum(personalia).medFaktum(nyeaddresse.medValue("true"));
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getTilflyttingsadresse()).isEqualTo(personalia.getProperties().get("gjeldendeAdresse"));
    }

    @Test
    public void settAvstand() {
        webSoknad = new WebSoknad().medFaktum(avstand);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getAvstand()).isEqualTo(new BigInteger(AVSTAND_KM));
    }

    @Test
    public void settFlyttingDekket() {
        webSoknad = new WebSoknad().medFaktum(flyttingDekket);
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.isErUtgifterTilFlyttingDekketAvAndreEnnNAV().booleanValue()).isEqualTo(true);
    }

    @Test
    public void settFlytteSelv() {
        when(messageSource.getMessage(eq(flytterselv.cms), isNull(Object[].class), eq(flytterselv.cms), any(Locale.class))).thenReturn("flytterselvcms");
        when(messageSource.getMessage(eq(flyttebyraa.cms), isNull(Object[].class), eq(flyttebyraa.cms), any(Locale.class))).thenReturn("flyttebyraacms");
        when(messageSource.getMessage(eq(tilbudmenflytterselv.cms), isNull(Object[].class), eq(tilbudmenflytterselv.cms), any(Locale.class))).thenReturn("tilbudmenflytterselvcms");

        webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("flytting.selvellerbistand").medValue("flytterselv"));
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getFlytterSelv()).isEqualTo("flytterselvcms");

        webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("flytting.selvellerbistand").medValue("flyttebyraa"));
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getFlytterSelv()).isEqualTo("flyttebyraacms");

        webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("flytting.selvellerbistand").medValue("tilbudmenflytterselv"));
        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getFlytterSelv()).isEqualTo("tilbudmenflytterselvcms");
    }

    @Test
    public void settSumTilleggsutgifter() {
        webSoknad = new WebSoknad()
                .medFaktum(hengerleie)
                .medFaktum(bom)
                .medFaktum(parkering)
                .medFaktum(ferge)
                .medFaktum(annet);

        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        Double sumTilleggsutgifter = new Double(HENGERLEIE_KR)
                + new Double(BOM_KR)
                + new Double(PARKERING_KR)
                + new Double(FERGE_KR)
                + new Double(ANNET_KR);

        assertThat(flytteutgifter.getSumTilleggsutgifter()).isEqualTo(sumTilleggsutgifter);
    }

    @Test
    public void settAnbud() {
        webSoknad = new WebSoknad()
                .medFaktum(forsteAnbudNavn)
                .medFaktum(forsteAnbudBelop)
                .medFaktum(andreAnbudNavn)
                .medFaktum(andreAnbudBelop)
                .medFaktum(valgtForste)
                .medFaktum(valgtAndre.medValue("false"))
        ;

        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);

        List<Anbud> anbudList = new ArrayList<>();
        anbudList.add(lagAnbud(NAVN_FLYTTEBYRAA_1, BELOP_FLYTTEBYRAA_1));
        anbudList.add(lagAnbud(NAVN_FLYTTEBYRAA_2, BELOP_FLYTTEBYRAA_2));

        assertThat(flytteutgifter.getAnbud().get(0).getFirmanavn()).isEqualTo(NAVN_FLYTTEBYRAA_1);
        assertThat(flytteutgifter.getAnbud().get(0).getTilbudsbeloep()).isEqualTo(new BigInteger(BELOP_FLYTTEBYRAA_1));
        assertThat(flytteutgifter.getAnbud().get(1).getFirmanavn()).isEqualTo(NAVN_FLYTTEBYRAA_2);
        assertThat(flytteutgifter.getAnbud().get(1).getTilbudsbeloep()).isEqualTo(new BigInteger(BELOP_FLYTTEBYRAA_2));
        assertThat(flytteutgifter.getValgtFlyttebyraa()).isEqualTo(NAVN_FLYTTEBYRAA_1);
    }

    @Test
    public void settAnbudAndreVvalgt() {
        webSoknad = new WebSoknad()
                .medFaktum(forsteAnbudNavn)
                .medFaktum(forsteAnbudBelop)
                .medFaktum(andreAnbudNavn)
                .medFaktum(andreAnbudBelop)
                .medFaktum(valgtForste.medValue("false"))
                .medFaktum(valgtAndre)
        ;

        flytteutgifter = flytteutgifterTilXml.transform(webSoknad);
        assertThat(flytteutgifter.getValgtFlyttebyraa()).isEqualTo(NAVN_FLYTTEBYRAA_2);
    }

    private Anbud lagAnbud(String navn, String belop) {
        Anbud anbud = new Anbud();
        anbud.setFirmanavn(navn);
        anbud.setTilbudsbeloep(new BigInteger(belop));

        return anbud;
    }

}
