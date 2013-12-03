package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import java.util.List;

/**
 * Map from TPS data format to internal domain model
 */
public class PersonTransform {

    public Person mapToPerson(Long soknadId, XMLHentKontaktinformasjonOgPreferanserResponse response, Kodeverk kodeverk) {
        
        if (response == null) {
            return new Person();
        }
        XMLBruker soapPerson = (XMLBruker) response.getPerson();
        Person person = new Person(
                soknadId,
                finnFnr(soapPerson),
                finnFornavn(soapPerson),
                finnMellomNavn(soapPerson),
                finnEtterNavn(soapPerson),
                finnGjeldendeAdressetype(soapPerson),
                finnAdresser(soknadId, soapPerson, kodeverk));
        person.setEpost(soknadId, finnEpost(soapPerson));
        
        return person;
    }

    private String finnEpost(XMLBruker soapPerson) {
        for (XMLElektroniskKommunikasjonskanal kanal : soapPerson.getElektroniskKommunikasjonskanal()) {
            if (kanal.getElektroniskAdresse() instanceof XMLEPost) {
                return ((XMLEPost) kanal.getElektroniskAdresse()).getIdentifikator();
            }
        }
        return null;
    }

    private String finnGjeldendeAdressetype(XMLBruker soapPerson) {
        return soapPerson.getGjeldendePostadresseType() != null ? soapPerson.getGjeldendePostadresseType().getValue() : "";
    }

    private List<Adresse> finnAdresser(long soknadId, XMLBruker soapPerson, Kodeverk kodeverk) {
        return new AdresseTransform().mapAdresser(soknadId, soapPerson, kodeverk);
    }

    private String finnFnr(XMLBruker soapPerson) {
        return soapPerson.getIdent().getIdent();
    }

    private String finnFornavn(XMLBruker soapPerson) {
        return fornavnExists(soapPerson) ? soapPerson.getPersonnavn().getFornavn() : "";
    }

    private boolean fornavnExists(XMLBruker soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getFornavn() != null;
    }

    private String finnMellomNavn(XMLBruker soapPerson) {
        return mellomnavnExists(soapPerson) ? soapPerson.getPersonnavn().getMellomnavn() : "";
    }

    private boolean mellomnavnExists(XMLBruker soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getMellomnavn() != null;
    }

    private String finnEtterNavn(XMLBruker soapPerson) {
        return etternavnExists(soapPerson) ? soapPerson.getPersonnavn().getEtternavn() : "";
    }

    private boolean etternavnExists(XMLBruker soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getEtternavn() != null;
    }
}
