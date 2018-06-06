package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.AdresseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class AdresseRessursTest {
    @Mock
    private SoknadService soknadService;
    @Mock
    private NorgService norgService;
    @Mock
    private AdresseService adresseService;

    private AdresseRessurs adresseRessurs = new AdresseRessurs();

    @Test
    public void mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontendMapperFelterKorrektForGateadresse() {
        AdresseForslag adresseForslagGateadresse = getAdresseForslagGateadresse();
        NavEnhet navEnhet = getNavEnhet();

        AdresseRessurs.NavEnhetFrontend navEnhetFrontend = adresseRessurs.mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslagGateadresse, navEnhet);

        assertThat(navEnhetFrontend.enhetsId, is(navEnhet.enhetNr));
        assertThat(navEnhetFrontend.enhetsnavn, is(navEnhet.navn));
        assertThat(navEnhetFrontend.kommunenummer, is(adresseForslagGateadresse.kommunenummer));
        assertThat(navEnhetFrontend.kommunenavn, is(adresseForslagGateadresse.kommunenavn));
        assertThat(navEnhetFrontend.bydelsnummer, is(adresseForslagGateadresse.bydel));
        assertThat(navEnhetFrontend.sosialOrgnr, is(navEnhet.sosialOrgnr));
        assertThat(navEnhetFrontend.features.size(), is(0));

    }

    @Test
    public void mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontendMapperFelterKorrektForMatrikkeladresse() {
        AdresseForslag adresseForslagMatrikkeladresse = getAdresseForslagMatrikkeladresse();
        NavEnhet navEnhet = getNavEnhet();

        AdresseRessurs.NavEnhetFrontend navEnhetFrontend = adresseRessurs.mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslagMatrikkeladresse, navEnhet);

        assertThat(navEnhetFrontend.enhetsId, is(navEnhet.enhetNr));
        assertThat(navEnhetFrontend.enhetsnavn, is(navEnhet.navn));
        assertThat(navEnhetFrontend.kommunenummer, is(adresseForslagMatrikkeladresse.kommunenummer));
        assertThat(navEnhetFrontend.kommunenavn, is(nullValue()));
        assertThat(navEnhetFrontend.bydelsnummer, is(nullValue()));
        assertThat(navEnhetFrontend.sosialOrgnr, is(navEnhet.sosialOrgnr));
        assertThat(navEnhetFrontend.features.size(), is(0));

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
        adresseForslagGateadresse.geografiskTilknytning = "0101";
        adresseForslagGateadresse.kommunenummer = "0100";
        adresseForslagGateadresse.bydel = "0102";
        adresseForslagGateadresse.kommunenavn = "Kommunen";
        adresseForslagGateadresse.type = "gateadresse";
        return adresseForslagGateadresse;
    }

    private AdresseForslag getAdresseForslagMatrikkeladresse() {
        AdresseForslag adresseForslagGateadresse = new AdresseForslag();
        adresseForslagGateadresse.geografiskTilknytning = "0101";
        adresseForslagGateadresse.kommunenummer = "0101";
        return adresseForslagGateadresse;
    }
}