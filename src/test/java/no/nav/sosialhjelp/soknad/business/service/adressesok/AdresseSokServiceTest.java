package no.nav.sosialhjelp.soknad.business.service.adressesok;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdresseSokServiceTest {

    private static final String EIER = "123456789101";
    private static final String KOMMUNENUMMER = "0300";
    private static final String GEOGRAFISK_TILKNYTNING = "0101";
    private static final String BYDEL = "0102";
    private static final String GATEADRESSE = "gateadresse";
    private static final String BOLIGNUMMER = "H0101";
    private static final String GATENAVN = "Sandakerveien";
    private static final String KOMMUNENAVN1 = "Kommune 1";
    private static final String LANDKODE = "NOR";
    private static final String POSTNUMMER = "0000";
    private static final String POSTSTED = "Oslo";
    private static final String HUSNUMMER = "53";
    private static final String HUSBOKSTAV = "B";

    @Mock
    private PdlAdresseSokService pdlAdresseSokService;

    @InjectMocks
    private AdresseSokService adresseSokService;

    @Test
    public void finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertGateadresse() {
        when(pdlAdresseSokService.getAdresseForslag(any())).thenReturn(lagAdresseForslag(KOMMUNENUMMER, KOMMUNENAVN1));
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        var personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setFolkeregistrertAdresse(createGateadresse());

        var adresseForslagene = adresseSokService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString());

        assertThat(adresseForslagene).hasSize(1);

        var adresseForslag = adresseForslagene.get(0);
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(GEOGRAFISK_TILKNYTNING);
        assertThat(adresseForslag.kommunenummer).isEqualTo(KOMMUNENUMMER);
        assertThat(adresseForslag.kommunenavn).isEqualTo(KOMMUNENAVN1);
        assertThat(adresseForslag.type).isEqualTo(GATEADRESSE);
    }

    @Test
    public void finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertMatrikkeladresse() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setFolkeregistrertAdresse(createMatrikkeladresse());

        List<AdresseForslag> adresseForslagene = adresseSokService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString());
        assertThat(adresseForslagene).hasSize(1);

        AdresseForslag adresseForslag = adresseForslagene.get(0);

        assertThat(adresseForslag.kommunenummer).isEqualTo(KOMMUNENUMMER);
        assertThat(adresseForslag.type).isEqualTo(AdresseForslagType.MATRIKKELADRESSE);
        // Får kun kommunenummer som adresseforslag. Ut fra denne finner man navenhet i den lokale lista
    }

    @Test
    public void finnAdresseFraSoknadReturnererTomListeHvisAdresseValgMangler() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        personalia.setOppholdsadresse(createGateadresse());

        List<AdresseForslag> adresseForslagene = adresseSokService.finnAdresseFraSoknad(personalia, null);

        assertThat(adresseForslagene).isEmpty();
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
}