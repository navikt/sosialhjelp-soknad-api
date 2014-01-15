package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class FamilieRelasjonTransform {
    private static final Logger logger = getLogger(FamilieRelasjonTransform.class);
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
                finnBarn(xmlperson, soknadId),
                finnStatsborgerskap(xmlperson));
    }
    
    private String finnStatsborgerskap(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        if(soapPerson.getStatsborgerskap() != null) {
            Statsborgerskap statsborgerskap = soapPerson.getStatsborgerskap();
            return statsborgerskap.getLand().getValue();
        }
        else {
            return "NOR";
        }
    }

    private List<Barn> finnBarn(
            no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson, Long soknadId) {
        List<Barn> result = new ArrayList<>();

        List<Familierelasjon> familierelasjoner = xmlperson.getHarFraRolleI();
        logger.warn("Informasjon om barn: " + familierelasjoner.size());
        if (familierelasjoner.isEmpty()) {
            logger.warn("Ingen barn");
            return result;
        }

        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            logger.warn("relasjonstype" + familierelasjonType.getValue());
            if (familierelasjonType.getValue().equals("BARN")) {
                no.nav.tjeneste.virksomhet.person.v1.informasjon.Person tilPerson = familierelasjon.getTilPerson();
                Barn barn = mapXmlPersonToPerson(tilPerson, soknadId);
                logger.warn("Barnets alder er " + barn.getAlder());
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
