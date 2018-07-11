package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;


public class BrukerprofilPortTypeMock implements BrukerprofilPortType {
    XMLBrukerMedSivilstatus person;

    public void setPerson(XMLBrukerMedSivilstatus person) {
        this.person = person;
    }

    public XMLBrukerMedSivilstatus getPerson() {
        return person;
    }

    @Override
    public void ping() {

    }

    @Override
    public XMLHentKontaktinformasjonOgPreferanserResponse hentKontaktinformasjonOgPreferanser(XMLHentKontaktinformasjonOgPreferanserRequest request) throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserResponse respons = new XMLHentKontaktinformasjonOgPreferanserResponse();
        respons.setPerson(person);
        return respons;
    }
}
