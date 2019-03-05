package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class SoknadsmottakerRessursTest {

    private SoknadsmottakerRessurs soknadsmottakerRessurs = new SoknadsmottakerRessurs();

    @Test
    public void mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontendMapperFelterKorrektForGateadresse() {
        AdresseForslag adresseForslagGateadresse = getAdresseForslagGateadresse();
        NavEnhet navEnhet = getNavEnhet();

        SoknadsmottakerRessurs.LegacyNavEnhetFrontend legacyNavEnhetFrontend = soknadsmottakerRessurs.mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslagGateadresse, navEnhet);

        assertThat(legacyNavEnhetFrontend.enhetsId, is(navEnhet.enhetNr));
        assertThat(legacyNavEnhetFrontend.enhetsnavn, is(navEnhet.navn));
        assertThat(legacyNavEnhetFrontend.kommunenummer, is(adresseForslagGateadresse.kommunenummer));
        assertThat(legacyNavEnhetFrontend.kommunenavn, is(adresseForslagGateadresse.kommunenavn));
        assertThat(legacyNavEnhetFrontend.bydelsnummer, is(adresseForslagGateadresse.bydel));
        assertThat(legacyNavEnhetFrontend.sosialOrgnr, is(navEnhet.sosialOrgnr));
        assertThat(legacyNavEnhetFrontend.features, is(notNullValue()));

    }

    @Test
    public void mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontendMapperFelterKorrektForMatrikkeladresse() {
        AdresseForslag adresseForslagMatrikkeladresse = getAdresseForslagMatrikkeladresse();
        NavEnhet navEnhet = getNavEnhet();

        SoknadsmottakerRessurs.LegacyNavEnhetFrontend legacyNavEnhetFrontend = soknadsmottakerRessurs.mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslagMatrikkeladresse, navEnhet);

        assertThat(legacyNavEnhetFrontend.enhetsId, is(navEnhet.enhetNr));
        assertThat(legacyNavEnhetFrontend.enhetsnavn, is(navEnhet.navn));
        assertThat(legacyNavEnhetFrontend.kommunenummer, is(adresseForslagMatrikkeladresse.kommunenummer));
        assertThat(legacyNavEnhetFrontend.kommunenavn, is(nullValue()));
        assertThat(legacyNavEnhetFrontend.bydelsnummer, is(nullValue()));
        assertThat(legacyNavEnhetFrontend.sosialOrgnr, is(navEnhet.sosialOrgnr));
        assertThat(legacyNavEnhetFrontend.features, is(notNullValue()));

    }

    private NavEnhet getNavEnhet() {
        NavEnhet navEnhet = new NavEnhet();
        navEnhet.enhetNr = "2000";
        navEnhet.navn = "Enheten";
        navEnhet.sosialOrgnr = "01010101";
        return navEnhet;
    }

    private AdresseForslag getAdresseForslagGateadresse() {
        AdresseForslag adresseForslagGateadresse = new AdresseForslag();
        adresseForslagGateadresse.geografiskTilknytning = "030101";
        adresseForslagGateadresse.kommunenummer = "0301";
        adresseForslagGateadresse.bydel = "030101";
        adresseForslagGateadresse.kommunenavn = "Kommunen";
        adresseForslagGateadresse.type = "gateadresse";
        return adresseForslagGateadresse;
    }

    private AdresseForslag getAdresseForslagMatrikkeladresse() {
        AdresseForslag adresseForslagGateadresse = new AdresseForslag();
        adresseForslagGateadresse.geografiskTilknytning = "030101";
        adresseForslagGateadresse.kommunenummer = "0301";
        return adresseForslagGateadresse;
    }
}