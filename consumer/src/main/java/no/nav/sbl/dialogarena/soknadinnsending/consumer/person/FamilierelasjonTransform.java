package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;


import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter.xmlPersonHarDiskresjonskode;

public class FamilierelasjonTransform {

    public static List<Barn> mapFamilierelasjon(HentKjerneinformasjonResponse response) {
        if (response == null) {
            return new ArrayList<>();
        }

        return finnBarn(response.getPerson());
    }

    private static List<Barn> finnBarn(Person xmlperson) {
        List<Barn> result = new ArrayList<>();

        List<Familierelasjon> familierelasjoner = xmlperson.getHarFraRolleI();
        if (familierelasjoner.isEmpty()) {
            return result;
        }
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (familierelasjonType.getValue().equals("BARN")) {
                Person xmlBarn = familierelasjon.getTilPerson();
                if (erMyndig(xmlBarn) && !erDoed(xmlBarn)) {
                    Barn barn = new Barn();
                    if (xmlPersonHarDiskresjonskode(xmlBarn)) {
                        barn.withIkkeTilgang(true);
                    } else {
                        barn = mapXmlBarnTilBarn(xmlBarn);
                        barn.withFolkeregistrertsammen(familierelasjon.isHarSammeBosted());
                        barn.withIkkeTilgang(false);
                    }
                    result.add(barn);
                }
            }
        }

        return result;
    }

    private static boolean erDoed(Person barn) {
        String personstatus = finnPersonstatus(barn);
        return barn.getDoedsdato() != null || (personstatus != null && "DØD".equals(personstatus));
    }

    private static boolean erMyndig(Person barn) {
        String fnr = finnFnr(barn);//Bruk fodselsdato i stedet!
        if (fnr != null) {
            PersonAlder personAlder = new PersonAlder(fnr);
            if (personAlder.getAlder() < 18) {
                return false;
            }
        }
        return true;
    }

    private static Barn mapXmlBarnTilBarn(Person xmlBarn) {
        return new Barn()
                .withFornavn(finnFornavn(xmlBarn))
                .withMellomnavn(finnMellomnavn(xmlBarn))
                .withEtternavn(finnEtternavn(xmlBarn))
                .withFnr(finnFnr(xmlBarn));
                //.withFodselsdato(xmlBarn.)
    }

    private static String finnPersonstatus(Person xmlperson) {
        Personstatus personstatus = xmlperson.getPersonstatus();
        if (personstatus != null && personstatus.getPersonstatus() != null) {
            return personstatus.getPersonstatus().getValue();
        }
        return "";
    }

    private static String finnFornavn(Person soapPerson) {
        return fornavnExists(soapPerson) ? soapPerson.getPersonnavn().getFornavn() : "";
    }

    private static boolean fornavnExists(Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getFornavn() != null;
    }

    private static String finnMellomnavn(Person soapPerson) {
        return mellomnavnExists(soapPerson) ? soapPerson.getPersonnavn().getMellomnavn() : "";
    }

    private static boolean mellomnavnExists(Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getMellomnavn() != null;
    }

    private static String finnEtternavn(Person soapPerson) {
        return etternavnExists(soapPerson) ? soapPerson.getPersonnavn().getEtternavn() : "";
    }

    private static boolean etternavnExists(Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getEtternavn() != null;
    }

    private static String finnFnr(Person xmlperson) {
        if (xmlperson.getIdent() == null) {
            return null;
        }
        return xmlperson.getIdent().getIdent();
    }
}
