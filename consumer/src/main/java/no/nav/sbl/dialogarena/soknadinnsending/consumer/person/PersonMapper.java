package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.mappers.PersonDataMapper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.joda.time.Years.yearsBetween;

public class PersonMapper {

    static final String RELASJON_EKTEFELLE = "EKTE";
    static final String RELASJON_REGISTRERT_PARTNER = "REPA";
    static final String RELASJON_BARN = "BARN";
    static final String KODE_6 = "SPSF";
    static final String KODE_7 = "SPFO";
    static final String KODE_6_TALLFORM = "6";
    static final String KODE_7_TALLFORM = "7";
    static final String DOED = "DØD";


    static Ektefelle finnEktefelleForPerson(Person xmlPerson) {
        final List<Familierelasjon> familierelasjoner = PersonDataMapper.finnFamilierelasjonerForPerson(xmlPerson);
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (RELASJON_EKTEFELLE.equals(familierelasjonType.getValue()) || RELASJON_REGISTRERT_PARTNER.equals(familierelasjonType.getValue())) {
                return mapFamilierelasjonTilEktefelle(familierelasjon);
            }
        }
        return null;
    }

    private static Ektefelle mapFamilierelasjonTilEktefelle(Familierelasjon familierelasjon) {
        Person xmlEktefelle = familierelasjon.getTilPerson();
        if (PersonDataMapper.xmlPersonHarDiskresjonskode(xmlEktefelle)) {
            return new Ektefelle()
                    .withIkketilgangtilektefelle(true);
        }
        return new Ektefelle()
                .withFornavn(PersonDataMapper.finnFornavn(xmlEktefelle))
                .withMellomnavn(PersonDataMapper.finnMellomnavn(xmlEktefelle))
                .withEtternavn(finnEtternavn(xmlEktefelle))
                .withFodselsdato(PersonDataMapper.finnFodselsdatoFraFnr(xmlEktefelle))
                .withFnr(finnFnr(xmlEktefelle))
                .withFolkeregistrertsammen(familierelasjon.isHarSammeBosted() != null ? familierelasjon.isHarSammeBosted() : false)
                .withIkketilgangtilektefelle(false);
    }


    public static String finnDiskresjonskode(Person xmlPerson) {
        if (xmlPerson.getDiskresjonskode() == null) {
            return null;
        }
        return xmlPerson.getDiskresjonskode().getValue();
    }

    public static boolean erDoed(Person xmlPerson) {
        String personstatus = finnPersonstatus(xmlPerson);
        return xmlPerson.getDoedsdato() != null || DOED.equals(personstatus);
    }

    private static String finnPersonstatus(Person xmlPerson) {
        Personstatus personstatus = xmlPerson.getPersonstatus();
        if (personstatus != null && personstatus.getPersonstatus() != null) {
            return personstatus.getPersonstatus().getValue();
        }
        return "";
    }

    public static boolean erMyndig(LocalDate fodselsdato) {
        return finnAlder(fodselsdato) >= 18;
    }

    private static int finnAlder(LocalDate fodselsdato) {
        if (fodselsdato != null) {
            return yearsBetween(fodselsdato, new LocalDate()).getYears();
        }
        return 0;
    }

    public static String finnFnr(Person person) {
        Aktoer aktoer = person.getAktoer();
        if (aktoer instanceof PersonIdent) {
            return kanskjeNorskIdent((PersonIdent) aktoer);
        }
        return null;
    }

    private static String kanskjeNorskIdent(PersonIdent aktoer) {
        return ofNullable(aktoer.getIdent())
                .map(NorskIdent::getIdent)
                .orElse(null);
    }

    public static boolean fornavnExists(Person xmlPerson) {
        return xmlPerson.getPersonnavn() != null && xmlPerson.getPersonnavn().getFornavn() != null;
    }

    public static boolean mellomnavnExists(Person xmlPerson) {
        return xmlPerson.getPersonnavn() != null && xmlPerson.getPersonnavn().getMellomnavn() != null;
    }

    public static String finnEtternavn(Person xmlPerson) {
        return etternavnExists(xmlPerson) ? xmlPerson.getPersonnavn().getEtternavn() : "";
    }

    private static boolean etternavnExists(Person xmlPerson) {
        return xmlPerson.getPersonnavn() != null && xmlPerson.getPersonnavn().getEtternavn() != null;
    }

    static String finnSammensattNavn(Person xmlPerson) {
        if (fornavnExists(xmlPerson) && mellomnavnExists(xmlPerson)) {
            return PersonDataMapper.finnFornavn(xmlPerson) + " " + PersonDataMapper.finnMellomnavn(xmlPerson) + " " + finnEtternavn(xmlPerson);
        } else if (fornavnExists(xmlPerson)) {
            return PersonDataMapper.finnFornavn(xmlPerson) + " " + finnEtternavn(xmlPerson);
        } else {
            return finnEtternavn(xmlPerson);
        }
    }


}
