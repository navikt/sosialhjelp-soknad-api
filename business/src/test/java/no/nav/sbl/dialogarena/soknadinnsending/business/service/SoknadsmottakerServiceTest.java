package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;

@RunWith(MockitoJUnitRunner.class)
public class SoknadsmottakerServiceTest {

    private static final String KOMMUNENUMMER = "0300";
    private static final String GEOGRAFISK_TILKNYTNING = "0101";
    private static final String BYDEL = "0102";
    private static final String GATEADRESSE = "gateadresse";
    private static final String BOLIGNUMMER = "H0101";
    private static final String GATENAVN = "Sandakerveien";
    private static final String KOMMUNENUMMER1 = "0100";
    private static final String KOMMUNENAVN1 = "Kommune 1";
    private static final String MATRIKKELADRESSE = "matrikkeladresse";
    private static final String LANDKODE = "NOR";
    private static final String KOMMUNENUMMER2 = "0200";
    private static final String KOMMUNENAVN2 = "Kommune 2";
    private static final String POSTNUMMER = "0000";
    private static final String POSTSTED = "Oslo";
    private static final String HUSNUMMER = "53";
    private static final String HUSBOKSTAV = "B";
    private static final String EIENDOMSNAVN = "Sandaker Gård";
    private static final String FESTENUMMER = "9876";

    @Mock
    private AdresseSokService adresseSokService;

    @InjectMocks
    private SoknadsmottakerService soknadsmottakerService;

    @Test
    public void hentAdresseFaktumGirFolkeregistrertAdresseHvisTypeErFolkeregistrert() {
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("folkeregistrert"))
                .medFaktum(lagFaktumForFolkeregistrertGateadresse());

        Faktum adresseFaktum = soknadsmottakerService.hentAdresseFaktum(webSoknad);

        assertThat(adresseFaktum.getProperties().size(), is(7));
        assertThat(adresseFaktum.getProperties().get("type"), is(GATEADRESSE));
    }

    @Test
    public void hentAdresseFaktumGirMidlertidigAdresseHvisTypeErMidleridig() {
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("midlertidig"))
                .medFaktum(lagFaktumForMidlertidigGateadresse());

        Faktum adresseFaktum = soknadsmottakerService.hentAdresseFaktum(webSoknad);

        assertThat(adresseFaktum.getProperties().size(), is(8));
        assertThat(adresseFaktum.getProperties().get("gatenavn"), is(GATENAVN));
    }

    @Test
    public void hentAdresseFaktumGirResultatFraAdresseSokHvisTypeErSoknad() {
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("soknad"))
                .medFaktum(lagFaktumForSoknadGateadresse());

        Faktum adresseFaktum = soknadsmottakerService.hentAdresseFaktum(webSoknad);

        assertThat(adresseFaktum.getProperties().size(), is(9));
        assertThat(adresseFaktum.getProperties().get("bolignummer"), is(BOLIGNUMMER));
    }

    @Test
    public void hentAdresseFaktumReturnererNullHvisAdresseFaktumMangler() {
        Faktum adresseFaktum = soknadsmottakerService.hentAdresseFaktum(new WebSoknad());

        assertThat(adresseFaktum, is(nullValue()));
    }

    @Test
    public void finnAdresseFraSoknadGirRiktigAdresseForMidlertidigGateadresse() {
        when(adresseSokService.sokEtterAdresser(any(Sokedata.class))).thenReturn(lagAdresseForslagListeMedEtInnslag());
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("midlertidig"))
                .medFaktum(lagFaktumForMidlertidigGateadresse());

        final List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(webSoknad);
        assertThat(adresseForslagene.size(), is(1));

        final AdresseForslag adresseForslag = adresseForslagene.get(0);
        assertThat(adresseForslag.geografiskTilknytning, is(GEOGRAFISK_TILKNYTNING));
        assertThat(adresseForslag.kommunenummer, is(KOMMUNENUMMER));
        assertThat(adresseForslag.kommunenavn, is(KOMMUNENAVN1));
        assertThat(adresseForslag.bydel, is(BYDEL));
        assertThat(adresseForslag.type, is(GATEADRESSE));
    }

    @Test
    public void finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertMatrikkeladresse() {
        when(adresseSokService.sokEtterNavKontor(any(Sokedata.class))).thenReturn(lagAdresseForslagListeMedEtInnslag());
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("folkeregistrert"))
                .medFaktum(lagFaktumForFolkeregistrertMatrikkeladresse());

        final List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(webSoknad);
        assertThat(adresseForslagene.size(), is(1));

        final AdresseForslag adresseForslag = adresseForslagene.get(0);

        assertThat(adresseForslag.geografiskTilknytning, is(GEOGRAFISK_TILKNYTNING));
        assertThat(adresseForslag.kommunenummer, is(KOMMUNENUMMER));
        assertThat(adresseForslag.kommunenavn, is(KOMMUNENAVN1));
    }

    @Test
    public void finnAdresseFraSoknadReturnererTomListeHvisAdressesokGirFlereResultater() {
        when(adresseSokService.sokEtterAdresser(any(Sokedata.class))).thenReturn(Arrays.asList(
            lagAdresseForslag(KOMMUNENUMMER1, KOMMUNENAVN1, "Foo"),
            lagAdresseForslag(KOMMUNENUMMER1, KOMMUNENAVN1, "Bar")
        ));
        
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("soknad"))
                .medFaktum(lagFaktumForSoknadGateadresse());

        final List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(webSoknad);

        assertThat(adresseForslagene, is(empty()));
    }
    
    @Test
    public void finnAdresseFraSoknadKanGiFlereNavKontor() {
        when(adresseSokService.sokEtterAdresser(any(Sokedata.class))).thenReturn(Arrays.asList(
            lagAdresseForslag(KOMMUNENUMMER1, KOMMUNENAVN1, "Foo"),
            lagAdresseForslag(KOMMUNENUMMER2, KOMMUNENAVN2, "Foo")
        ));
        
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("soknad"))
                .medFaktum(lagFaktumForSoknadGateadresse());

        final List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(webSoknad);

        assertThat(adresseForslagene.size(), is(2));
    }

    @Test
    public void finnAdresseFraSoknadReturnererTomListeHvisAdresseFaktumMangler() {
        List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(new WebSoknad());

        assertThat(adresseForslagene, is(empty()));
    }

    private Faktum lagFaktumForFolkeregistrertGateadresse() {
        return new Faktum()
                .medKey("kontakt.system.folkeregistrert.adresse")
                .medSystemProperty("type", GATEADRESSE)
                .medSystemProperty("landkode", LANDKODE)
                .medSystemProperty("kommunenummer", KOMMUNENUMMER)
                .medSystemProperty("postnummer", POSTNUMMER)
                .medSystemProperty("poststed", POSTSTED)
                .medSystemProperty("gatenavn", GATENAVN)
                .medSystemProperty("husnummer", HUSNUMMER);
    }

    private Faktum lagFaktumForFolkeregistrertMatrikkeladresse() {
        return new Faktum()
                .medKey("kontakt.system.folkeregistrert.adresse")
                .medSystemProperty("type", MATRIKKELADRESSE)
                .medSystemProperty("landkode", LANDKODE)
                .medSystemProperty("kommunenummer", KOMMUNENUMMER)
                .medSystemProperty("postnummer", POSTNUMMER)
                .medSystemProperty("poststed", POSTSTED)
                .medSystemProperty("eiendomsnavn", EIENDOMSNAVN)
                .medSystemProperty("festenummer", FESTENUMMER);
    }

    private Faktum lagFaktumForMidlertidigGateadresse() {
        return new Faktum()
                .medKey("kontakt.system.adresse")
                .medSystemProperty("type", GATEADRESSE)
                .medSystemProperty("landkode", LANDKODE)
                .medSystemProperty("kommunenummer", KOMMUNENUMMER)
                .medSystemProperty("postnummer", POSTNUMMER)
                .medSystemProperty("poststed", POSTSTED)
                .medSystemProperty("gatenavn", GATENAVN)
                .medSystemProperty("husnummer", HUSNUMMER)
                .medSystemProperty("husbokstav", HUSBOKSTAV);
    }

    private Faktum lagFaktumForSoknadGateadresse() {
        return new Faktum()
                .medKey("kontakt.adresse.bruker")
                .medSystemProperty("type", GATEADRESSE)
                .medSystemProperty("landkode", LANDKODE)
                .medSystemProperty("kommunenummer", KOMMUNENUMMER)
                .medSystemProperty("postnummer", POSTNUMMER)
                .medSystemProperty("poststed", POSTSTED)
                .medSystemProperty("gatenavn", GATENAVN)
                .medSystemProperty("husnummer", HUSNUMMER)
                .medSystemProperty("husbokstav", HUSBOKSTAV)
                .medSystemProperty("bolignummer", BOLIGNUMMER);
    }

    private List<AdresseForslag> lagAdresseForslagListeMedFlereInnslag() {
        List<AdresseForslag> adresseForslagListe = new ArrayList<>();
        adresseForslagListe.add(lagAdresseForslag(KOMMUNENUMMER1, KOMMUNENAVN1));
        adresseForslagListe.add(lagAdresseForslag(KOMMUNENUMMER2, KOMMUNENAVN2));
        return adresseForslagListe;
    }
    
    private AdresseForslag lagAdresseForslag(String kommunenummer, String kommunenavn) {
        return lagAdresseForslag(kommunenummer, kommunenavn, "Gateveien");
    }
    
    private AdresseForslag lagAdresseForslag(String kommunenummer, String kommunenavn, String adresse) {
        AdresseForslag adresseForslag = new AdresseForslag();
        adresseForslag.geografiskTilknytning = GEOGRAFISK_TILKNYTNING;
        adresseForslag.kommunenummer = kommunenummer;
        adresseForslag.bydel = BYDEL;
        adresseForslag.kommunenavn = kommunenavn;
        adresseForslag.adresse = adresse;
        adresseForslag.postnummer = "0030";
        adresseForslag.poststed = "Mocka";
        adresseForslag.type = GATEADRESSE;
        return adresseForslag;
    }

    private List<AdresseForslag> lagAdresseForslagListeMedEtInnslag() {
        List<AdresseForslag> adresseForslagListe = new ArrayList<>();
        adresseForslagListe.add(lagAdresseForslag(KOMMUNENUMMER, KOMMUNENAVN1));
        return adresseForslagListe;
    }
}