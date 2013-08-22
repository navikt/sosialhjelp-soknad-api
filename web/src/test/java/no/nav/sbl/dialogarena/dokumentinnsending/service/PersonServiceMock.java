package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.AdresseType;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.PersonProfil;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPerson;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import javax.jws.WebParam;
import java.util.HashMap;
import java.util.Map;

/**
 * Helt enkel mock klasse som bare sjekker om ident som kommer inn er gyldig.
 * Returnerer et Person objekt med e-post og norsk adresse satt.
 * Må utvides om en ønsker å kunne returnere forskjellige typer.
 *
 * @author j139113
 */
public class PersonServiceMock implements BrukerprofilPortType {

    private Map<String, XMLHentKontaktinformasjonOgPreferanserResponse> brukerProfiler = new HashMap<>();

    public void ping() {
    }

    public XMLHentKontaktinformasjonOgPreferanserResponse hentKontaktinformasjonOgPreferanser(
            @WebParam(name = "request", targetNamespace = "") XMLHentKontaktinformasjonOgPreferanserRequest request)
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {

        XMLHentKontaktinformasjonOgPreferanserResponse response = brukerProfiler.get(request.getIdent());

        if (response == null) {
            response = lagPersonMedFastNorskAdresse();
        }

        return response;
    }

    public XMLHentKontaktinformasjonOgPreferanserResponse lagPersonMedFastNorskAdresse() {
        return new XMLHentKontaktinformasjonOgPreferanserResponse().
                withPerson(makePerson(PersonProfil.lagPersonBosattINorgeUtenEpost("12345612345")));
    }

    public void stub(PersonProfil rad) {
        brukerProfiler.put(rad.ident, makeXMLReponse(rad));
    }

    private XMLHentKontaktinformasjonOgPreferanserResponse makeXMLReponse(PersonProfil rad) {
        return new XMLHentKontaktinformasjonOgPreferanserResponse().withPerson(makePerson(rad));
    }

    private XMLPerson makePerson(PersonProfil rad) {
        return new XMLBruker()
                .withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal(rad)).withGjeldendePostadresseType(lagGjeldendeAdresseType(rad.adresseType));
    }

    private XMLPostadressetyper lagGjeldendeAdresseType(AdresseType adresseType) {
        AdresseType tmpAdresseType = adresseType;
        if (null == tmpAdresseType) {
            tmpAdresseType = AdresseType.BOSTEDSADRESSE;
        }
        return new XMLPostadressetyper().withValue(tmpAdresseType.name());
    }

    public static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal(PersonProfil rad) {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse(rad.epost));
    }


    public static XMLElektroniskAdresse lagElektroniskAdresse(String epost) {
        return new XMLEPost().withIdentifikator(epost);
    }
}