package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.StatsborgerskapDto;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.springframework.stereotype.Component;

@Component
public class PdlPersonMapper {

    private static final String KODE_6 = "SPSF";
    private static final String KODE_7 = "SPFO";

    public Person mapTilPerson(PdlPerson pdlPerson, String ident) {
        if (pdlPerson == null) {
            return null;
        }
        return new Person()
                .withFornavn(finnFornavn(pdlPerson))
                .withMellomnavn(finnMellomnavn(pdlPerson))
                .withEtternavn(finnEtternavn(pdlPerson))
                .withFnr(ident)
                .withSivilstatus(finnSivilstatus(pdlPerson))
                .withStatsborgerskap(finnStatsborgerskap(pdlPerson))
                .withDiskresjonskode(finnAdressebeskyttelse(pdlPerson));
    }

    public Barn mapTilBarn(PdlPerson pdlBarn, String barnIdent, PdlPerson pdlPerson) {
        if (pdlBarn.harAdressebeskyttelse()) {
            return null;
        }
        if (erMyndig(pdlBarn) || erDoed(pdlBarn)) {
            return null;
        }
        return new Barn()
                .withFornavn(finnFornavn(pdlBarn))
                .withMellomnavn(finnMellomnavn(pdlBarn))
                .withEtternavn(finnEtternavn(pdlBarn))
                .withFnr(barnIdent)
                .withFodselsdato(finnFodselsdato(pdlBarn))
                .withFolkeregistrertsammen(erFolkeregistrertSammen(pdlPerson, pdlBarn));
    }

    public Ektefelle mapTilEktefelle(PdlPerson pdlEktefelle, String ektefelleIdent, PdlPerson pdlPerson) {
        if (pdlEktefelle.harAdressebeskyttelse()) {
            return new Ektefelle()
                    .withIkketilgangtilektefelle(true);
        }
        return new Ektefelle()
                .withFornavn(finnFornavn(pdlEktefelle))
                .withMellomnavn(finnMellomnavn(pdlEktefelle))
                .withEtternavn(finnEtternavn(pdlEktefelle))
                .withFnr(ektefelleIdent)
                .withFodselsdato(finnFodselsdato(pdlEktefelle))
                .withIkketilgangtilektefelle(false)
                .withFolkeregistrertsammen(erFolkeregistrertSammen(pdlPerson, pdlEktefelle));
    }

    private String finnFornavn(PdlPerson pdlPerson) {
        return pdlPerson.getNavn().stream()
                .findFirst().map(NavnDto::getFornavn).orElse("");
    }

    private String finnMellomnavn(PdlPerson pdlPerson) {
        return pdlPerson.getNavn().stream()
                .findFirst().map(NavnDto::getMellomnavn).orElse("");
    }

    private String finnEtternavn(PdlPerson pdlPerson) {
        return pdlPerson.getNavn().stream()
                .findFirst().map(NavnDto::getEtternavn).orElse("");
    }

    private LocalDate finnFodselsdato(PdlPerson pdlPerson) {
        return pdlPerson.getFoedsel().stream().findFirst()
                .map(foedselDto -> new LocalDate(foedselDto.getFoedselsdato().getYear(), foedselDto.getFoedselsdato().getMonthValue(), foedselDto.getFoedselsdato().getDayOfMonth()))
                .orElse(null);
    }

    private boolean erMyndig(PdlPerson pdlPerson) {
        return finnAlder(pdlPerson) >= 18;
    }

    private int finnAlder(PdlPerson pdlPerson) {
        LocalDate foedselsdato = finnFodselsdato(pdlPerson);
        if (foedselsdato == null) {
            return 0;
        }
        return Years.yearsBetween(foedselsdato, LocalDate.now()).getYears();
    }

    private boolean erDoed(PdlPerson pdlPerson) {
        return pdlPerson.getFolkeregisterpersonstatus().stream().findFirst()
                .map(it -> "DOED".equalsIgnoreCase(it.getStatus()))
                .orElse(false);
    }

    private String finnSivilstatus(PdlPerson pdlPerson) {
        return pdlPerson.getSivilstand().stream().findFirst()
                .map(dto -> dto.getType().name())
                .orElse("");
    }

    private String finnStatsborgerskap(PdlPerson pdlPerson) {
        return pdlPerson.getStatsborgerskap().stream().findFirst()
                .map(StatsborgerskapDto::getLand)
                .orElse("NOR");
    }

    private String finnAdressebeskyttelse(PdlPerson pdlPerson) {
        return pdlPerson.getAdressebeskyttelse().stream().findFirst()
                .map(dto -> mapTilDiskresjonskode(dto.getGradering()))
                .orElse(null);
    }

    private String mapTilDiskresjonskode(AdressebeskyttelseDto.Gradering gradering) {
        if (gradering == AdressebeskyttelseDto.Gradering.UGRADERT) {
            return null;
        } else if (gradering == AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG || gradering == AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG_UTLAND) {
            return KODE_6;
        } else if (gradering == AdressebeskyttelseDto.Gradering.FORTROLIG) {
            return KODE_7;
        } else {
            return null;
        }
    }

    private boolean erFolkeregistrertSammen(PdlPerson pdlPerson, PdlPerson pdlBarnEllerEktefelle){
        //todo fix - sjekke om person og barnEllerEktefelle har samme adresse.
        return true;
    }
}
