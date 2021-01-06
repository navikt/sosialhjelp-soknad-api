package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslagType;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadsmottakerServiceTest {
    private static final String EIER = "123456789101";

    private static final String KOMMUNENUMMER = "0300";
    private static final String GEOGRAFISK_TILKNYTNING = "0101";
    private static final String BYDEL = "0102";
    private static final String GATEADRESSE = "gateadresse";
    private static final String BOLIGNUMMER = "H0101";
    private static final String GATENAVN = "Sandakerveien";
    private static final String KOMMUNENUMMER1 = "0100";
    private static final String KOMMUNENAVN1 = "Kommune 1";
    private static final String LANDKODE = "NOR";
    private static final String KOMMUNENUMMER2 = "0200";
    private static final String KOMMUNENAVN2 = "Kommune 2";
    private static final String POSTNUMMER = "0000";
    private static final String POSTSTED = "Oslo";
    private static final String HUSNUMMER = "53";
    private static final String HUSBOKSTAV = "B";

    @Mock
    private AdresseSokService adresseSokService;

    @InjectMocks
    private SoknadsmottakerService soknadsmottakerService;

    @Test
    public void finnAdresseFraSoknadGirRiktigAdresseForMidlertidigGateadresse() {
        when(adresseSokService.sokEtterAdresser(any(Sokedata.class))).thenReturn(lagAdresseForslagListeMedEtInnslag());
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setOppholdsadresse(createGateadresse());

        List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(personalia, JsonAdresseValg.MIDLERTIDIG.toString());
        assertThat(adresseForslagene.size(), is(1));

        AdresseForslag adresseForslag = adresseForslagene.get(0);
        assertThat(adresseForslag.geografiskTilknytning, is(GEOGRAFISK_TILKNYTNING));
        assertThat(adresseForslag.kommunenummer, is(KOMMUNENUMMER));
        assertThat(adresseForslag.kommunenavn, is(KOMMUNENAVN1));
        assertThat(adresseForslag.bydel, is(BYDEL));
        assertThat(adresseForslag.type, is(GATEADRESSE));
    }

    @Test
    public void finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertMatrikkeladresse() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setFolkeregistrertAdresse(createMatrikkeladresse());

        List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString());
        assertThat(adresseForslagene.size(), is(1));

        AdresseForslag adresseForslag = adresseForslagene.get(0);

        assertThat(adresseForslag.kommunenummer, is(KOMMUNENUMMER));
        assertThat(adresseForslag.type, is(AdresseForslagType.matrikkelAdresse));
        // Får kun kommunenummer som adresseforslag. Ut fra denne finner man navenhet i den lokale lista
    }

    @Test
    public void finnAdresseFraSoknadReturnererTomListeHvisAdressesokGirFlereResultater() {
        when(adresseSokService.sokEtterAdresser(any(Sokedata.class))).thenReturn(Arrays.asList(
            lagAdresseForslag(KOMMUNENUMMER1, KOMMUNENAVN1, "Foo"),
            lagAdresseForslag(KOMMUNENUMMER1, KOMMUNENAVN1, "Bar")
        ));

        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setOppholdsadresse(createGateadresse());

        List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(personalia, JsonAdresseValg.SOKNAD.toString());

        assertThat(adresseForslagene, is(empty()));
    }
    
    @Test
    public void finnAdresseFraSoknadKanGiFlereNavKontor() {
        when(adresseSokService.sokEtterAdresser(any(Sokedata.class))).thenReturn(Arrays.asList(
            lagAdresseForslag(KOMMUNENUMMER1, KOMMUNENAVN1, "Foo"),
            lagAdresseForslag(KOMMUNENUMMER2, KOMMUNENAVN2, "Foo")
        ));

        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setOppholdsadresse(createGateadresse());

        List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(personalia, JsonAdresseValg.SOKNAD.toString());

        assertThat(adresseForslagene.size(), is(2));
    }

    @Test
    public void finnAdresseFraSoknadReturnererTomListeHvisAdresseValgMangler() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setOppholdsadresse(createGateadresse());

        List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(personalia, null);

        assertThat(adresseForslagene, is(empty()));
    }

    private JsonAdresse createMatrikkeladresse(){
        return new JsonMatrikkelAdresse()
                .withType(JsonAdresse.Type.MATRIKKELADRESSE)
                .withKommunenummer(KOMMUNENUMMER);
    }

    private JsonAdresse createGateadresse(){
        return new JsonGateAdresse()
                .withType(JsonAdresse.Type.GATEADRESSE)
                .withLandkode(LANDKODE)
                .withKommunenummer(KOMMUNENUMMER)
                .withPostnummer(POSTNUMMER)
                .withPoststed(POSTSTED)
                .withGatenavn(GATENAVN)
                .withHusnummer(HUSNUMMER)
                .withHusbokstav(HUSBOKSTAV)
                .withBolignummer(BOLIGNUMMER);
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