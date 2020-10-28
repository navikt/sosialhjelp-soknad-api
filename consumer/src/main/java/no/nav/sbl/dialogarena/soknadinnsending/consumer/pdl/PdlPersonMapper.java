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
                .withDiskresjonskode(finnAdressebeskyttelse(pdlPerson))
                .withEktefelle(finnEktefelle(pdlPerson));
    }

    public Barn mapTilBarn(PdlPerson pdlPerson, String ident) {
        //TODO
        return null;
    }

    public Ektefelle mapTilEktefelle(PdlPerson pdlPerson, String ident) {
        //TODO
        return null;
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

    private int finnAlder(PdlPerson pdlPerson) {
        LocalDate foedselsdato = finnFodselsdato(pdlPerson);
        if (foedselsdato == null) {
            return 0;
        }
        return Years.yearsBetween(foedselsdato, LocalDate.now()).getYears();
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

    private Ektefelle finnEktefelle(PdlPerson pdlPerson) {
        return null;
    }

}
