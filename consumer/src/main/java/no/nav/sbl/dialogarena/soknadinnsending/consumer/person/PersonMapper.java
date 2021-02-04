package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import com.google.common.collect.ImmutableMap;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.NavFodselsnummer;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personstatus;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.joda.time.Years.yearsBetween;
import static org.slf4j.LoggerFactory.getLogger;

public class PersonMapper {

    private static final Logger log = getLogger(PersonMapper.class);

    static final String RELASJON_EKTEFELLE = "EKTE";
    static final String RELASJON_REGISTRERT_PARTNER = "REPA";
    static final String RELASJON_BARN = "BARN";
    static final String KODE_6 = "SPSF";
    static final String KODE_7 = "SPFO";
    static final String KODE_6_TALLFORM = "6";
    static final String KODE_7_TALLFORM = "7";
    static final String DOED = "DØD";

    private static final Map<String, String> MAP_XMLSIVILSTATUS_TIL_JSONSIVILSTATUS = new ImmutableMap.Builder<String, String>()
            .put("GIFT", "gift")
            .put("GLAD", "gift")
            .put("REPA", "gift")
            .put("SAMB", "samboer")
            .put("UGIF", "ugift")
            .put("ENKE", "enke")
            .put("GJPA", "enke")
            .put("SEPA", "separert")
            .put("SEPR", "separert")
            .put("SKIL", "skilt")
            .put("SKPA", "skilt").build();

    static no.nav.sbl.dialogarena.sendsoknad.domain.Person mapXmlPersonTilPerson(Person xmlPerson) {
        if (xmlPerson == null) {
            return null;
        }
        return new no.nav.sbl.dialogarena.sendsoknad.domain.Person()
                .withFornavn(finnFornavn(xmlPerson))
                .withMellomnavn(finnMellomnavn(xmlPerson))
                .withEtternavn(finnEtternavn(xmlPerson))
                .withFnr(finnFnr(xmlPerson))
                .withSivilstatus(finnSivilstatus(xmlPerson))
                .withStatsborgerskap(List.of(finnStatsborgerskap(xmlPerson)))
                .withDiskresjonskode(finnDiskresjonskode(xmlPerson))
                .withEktefelle(finnEktefelleForPerson(xmlPerson));
    }

    static Ektefelle finnEktefelleForPerson(Person xmlPerson) {
        final List<Familierelasjon> familierelasjoner = finnFamilierelasjonerForPerson(xmlPerson);
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (RELASJON_EKTEFELLE.equals(familierelasjonType.getValue()) || RELASJON_REGISTRERT_PARTNER.equals(familierelasjonType.getValue())) {
                return mapFamilierelasjonTilEktefelle(familierelasjon);
            }
        }
        return null;
    }

    private static List<Familierelasjon> finnFamilierelasjonerForPerson(Person xmlPerson) {
        List<Familierelasjon> familierelasjoner = xmlPerson.getHarFraRolleI();
        if (familierelasjoner == null || familierelasjoner.isEmpty()) {
            return new ArrayList<>();
        }
        return familierelasjoner;
    }

    private static Ektefelle mapFamilierelasjonTilEktefelle(Familierelasjon familierelasjon) {
        Person xmlEktefelle = familierelasjon.getTilPerson();
        if (xmlPersonHarDiskresjonskode(xmlEktefelle)) {
            return new Ektefelle()
                    .withIkketilgangtilektefelle(true);
        }
        return new Ektefelle()
                .withFornavn(finnFornavn(xmlEktefelle))
                .withMellomnavn(finnMellomnavn(xmlEktefelle))
                .withEtternavn(finnEtternavn(xmlEktefelle))
                .withFodselsdato(finnFodselsdatoFraFnr(xmlEktefelle))
                .withFnr(finnFnr(xmlEktefelle))
                .withFolkeregistrertsammen(familierelasjon.isHarSammeBosted() != null ? familierelasjon.isHarSammeBosted() : false)
                .withIkketilgangtilektefelle(false);
    }

    static List<Barn> finnBarnForPerson(Person xmlPerson) {
        final List<Familierelasjon> familierelasjoner = finnFamilierelasjonerForPerson(xmlPerson);
        List<Barn> alleBarn = new ArrayList<>();
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (RELASJON_BARN.equals(familierelasjonType.getValue())) {
                alleBarn.add(mapFamilierelasjonTilBarn(familierelasjon));
            }
        }
        alleBarn.removeIf(Objects::isNull);
        return alleBarn;
    }


    private static Barn mapFamilierelasjonTilBarn(Familierelasjon familierelasjon) {
        Person xmlBarn = familierelasjon.getTilPerson();
        if (xmlPersonHarDiskresjonskode(xmlBarn)) {
            return null;
        }

        if (!erMyndig(finnFodselsdatoFraFnr(xmlBarn)) && !erDoed(xmlBarn)) {
            return new Barn()
                    .withFornavn(finnFornavn(xmlBarn))
                    .withMellomnavn(finnMellomnavn(xmlBarn))
                    .withEtternavn(finnEtternavn(xmlBarn))
                    .withFnr(finnFnr(xmlBarn))
                    .withFodselsdato(finnFodselsdatoFraFnr(xmlBarn))
                    .withFolkeregistrertsammen(familierelasjon.isHarSammeBosted() != null ? familierelasjon.isHarSammeBosted() : false);
        } else {
            return null;
        }
    }

    static boolean xmlPersonHarDiskresjonskode(Person xmlPerson) {
        final String diskresjonskode = finnDiskresjonskode(xmlPerson);
        return KODE_6_TALLFORM.equalsIgnoreCase(diskresjonskode) || KODE_6.equalsIgnoreCase(diskresjonskode)
                || KODE_7_TALLFORM.equalsIgnoreCase(diskresjonskode) || KODE_7.equalsIgnoreCase(diskresjonskode);
    }

    private static String finnDiskresjonskode(Person xmlPerson) {
        if (xmlPerson.getDiskresjonskode() == null) {
            return null;
        }
        return xmlPerson.getDiskresjonskode().getValue();
    }

    static boolean erDoed(Person xmlPerson) {
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

    static boolean erMyndig(LocalDate fodselsdato) {
        return finnAlder(fodselsdato) >= 18;
    }

    private static int finnAlder(LocalDate fodselsdato) {
        if (fodselsdato != null) {
            return yearsBetween(fodselsdato, new LocalDate()).getYears();
        }
        return 0;
    }

    private static LocalDate finnFodselsdatoFraFnr(Person xmlPerson) {
        if (xmlPerson.getIdent() == null || xmlPerson.getIdent().getType() == null) {
            return null;
        }
        String identtype = xmlPerson.getIdent().getType().getValue();
        String ident = xmlPerson.getIdent().getIdent();

        if (isNotEmpty(ident) && ident.length() == 11 && ident.substring(6).equalsIgnoreCase("00000")) {
            log.info("Ektefelleident fra Person_v1 er FDAT, men identtype = {}", identtype);
        }

        if ("FNR".equalsIgnoreCase(identtype) && isNotEmpty(ident)) {
            NavFodselsnummer fnr = new NavFodselsnummer(xmlPerson.getIdent().getIdent());
            return new LocalDate(fnr.getBirthYear() + "-" + fnr.getMonth() + "-" + fnr.getDayInMonth());
        }
        return null;
    }

    public static String finnSivilstatus(Person xmlPerson) {
        if (xmlPerson.getSivilstand() == null || xmlPerson.getSivilstand().getSivilstand() == null) {
            return null;
        }
        return MAP_XMLSIVILSTATUS_TIL_JSONSIVILSTATUS.get(xmlPerson.getSivilstand().getSivilstand().getValue());
    }

    private static String finnFornavn(Person xmlPerson) {
        return fornavnExists(xmlPerson) ? xmlPerson.getPersonnavn().getFornavn() : "";
    }

    private static boolean fornavnExists(Person xmlPerson) {
        return xmlPerson.getPersonnavn() != null && xmlPerson.getPersonnavn().getFornavn() != null;
    }

    private static String finnMellomnavn(Person xmlPerson) {
        return mellomnavnExists(xmlPerson) ? xmlPerson.getPersonnavn().getMellomnavn() : "";
    }

    private static boolean mellomnavnExists(Person xmlPerson) {
        return xmlPerson.getPersonnavn() != null && xmlPerson.getPersonnavn().getMellomnavn() != null;
    }

    private static String finnEtternavn(Person xmlPerson) {
        return etternavnExists(xmlPerson) ? xmlPerson.getPersonnavn().getEtternavn() : "";
    }

    private static boolean etternavnExists(Person xmlPerson) {
        return xmlPerson.getPersonnavn() != null && xmlPerson.getPersonnavn().getEtternavn() != null;
    }

    private static String finnFnr(Person xmlPerson) {
        if (xmlPerson.getIdent() == null) {
            return null;
        }
        return xmlPerson.getIdent().getIdent();
    }

    private static String finnStatsborgerskap(Person xmlPerson) {
        if (xmlPerson.getStatsborgerskap() != null && xmlPerson.getStatsborgerskap().getLand() != null) {
            Statsborgerskap statsborgerskap = xmlPerson.getStatsborgerskap();
            return statsborgerskap.getLand().getValue();
        } else {
            return "NOR";
        }
    }
}
