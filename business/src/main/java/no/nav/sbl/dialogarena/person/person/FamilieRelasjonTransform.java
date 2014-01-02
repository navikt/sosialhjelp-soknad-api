package no.nav.sbl.dialogarena.person.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import java.util.ArrayList;
import java.util.List;

public class FamilieRelasjonTransform {

    public Person mapFamilierelasjonTilPerson(Long soknadId,
                                              HentKjerneinformasjonResponse response) {
        if (response == null) {
            return new Person();
        }

        no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson = response.getPerson();

        return new Person(
                soknadId,
                finnFnr(xmlperson),
                finnFornavn(xmlperson),
                finnMellomNavn(xmlperson),
                finnEtterNavn(xmlperson),
                finnBarn(xmlperson, soknadId));
    }

    private List<Barn> finnBarn(
            no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson, Long soknadId) {
        List<Barn> result = new ArrayList<>();

        List<Familierelasjon> familierelasjoner = xmlperson.getHarFraRolleI();
        if (familierelasjoner.isEmpty()) {
            return result;
        }

        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();

            if (familierelasjonType.getValue().equals("FARA") || familierelasjonType.getValue().equals("MORA")) {
                no.nav.tjeneste.virksomhet.person.v1.informasjon.Person tilPerson = familierelasjon.getTilPerson();
                Barn barn = mapXmlPersonToPerson(tilPerson, soknadId);
                if (barn.getAlder() < 18) {
                    result.add(barn);
                }
            }
        }

        return result;
    }

    private Barn mapXmlPersonToPerson(
            no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson, Long soknadId) {
        return new Barn(
                soknadId,
                finnFnr(xmlperson),
                finnFornavn(xmlperson),
                finnMellomNavn(xmlperson),
                finnEtterNavn(xmlperson));
    }

    private String finnFornavn(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return fornavnExists(soapPerson) ? soapPerson.getPersonnavn().getFornavn() : "";
    }

    private boolean fornavnExists(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getFornavn() != null;
    }

    private String finnMellomNavn(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return mellomnavnExists(soapPerson) ? soapPerson.getPersonnavn().getMellomnavn() : "";
    }

    private boolean mellomnavnExists(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getMellomnavn() != null;
    }

    private String finnEtterNavn(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return etternavnExists(soapPerson) ? soapPerson.getPersonnavn().getEtternavn() : "";
    }

    private boolean etternavnExists(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getEtternavn() != null;
    }

    private String finnFnr(
            no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson) {
        return xmlperson.getIdent().getIdent();
    }
}
