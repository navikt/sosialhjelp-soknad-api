package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.BostedsadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FoedselDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FolkeregisterpersonstatusDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.StatsborgerskapDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto.Gradering.UGRADERT;

@Component
public class PdlPersonMapper {

    static final String KODE_6 = "SPSF";
    static final String KODE_7 = "SPFO";
    public static final String NOR = "NOR";
    public static final String DOED = "DOED";

    public Person mapTilPerson(PdlPerson pdlPerson, String ident) {
        if (pdlPerson == null) {
            return null;
        }
        return new Person()
                .withFornavn(finnFornavn(pdlPerson.getNavn()))
                .withMellomnavn(finnMellomnavn(pdlPerson.getNavn()))
                .withEtternavn(finnEtternavn(pdlPerson.getNavn()))
                .withFnr(ident)
                .withSivilstatus(finnSivilstatus(pdlPerson.getSivilstand()))
                .withStatsborgerskap(finnStatsborgerskap(pdlPerson.getStatsborgerskap()))
                .withDiskresjonskode(finnAdressebeskyttelse(pdlPerson.getAdressebeskyttelse()));
    }

    public Barn mapTilBarn(PdlBarn pdlBarn, String barnIdent, PdlPerson pdlPerson) {
        if (harAdressebeskyttelse(pdlBarn.getAdressebeskyttelse())) {
            return null;
        }
        if (erMyndig(pdlBarn.getFoedsel()) || erDoed(pdlBarn.getFolkeregisterpersonstatus())) {
            return null;
        }
        return new Barn()
                .withFornavn(finnFornavn(pdlBarn.getNavn()))
                .withMellomnavn(finnMellomnavn(pdlBarn.getNavn()))
                .withEtternavn(finnEtternavn(pdlBarn.getNavn()))
                .withFnr(barnIdent)
                .withFodselsdato(finnFodselsdato(pdlBarn.getFoedsel()))
                .withFolkeregistrertsammen(erFolkeregistrertSammen(pdlPerson.getBostedsadresse(), pdlBarn.getBostedsadresse()));
    }

    public Ektefelle mapTilEktefelle(PdlEktefelle pdlEktefelle, String ektefelleIdent, PdlPerson pdlPerson) {
        if (pdlEktefelle == null) {
            return null;
        }
        if (harAdressebeskyttelse(pdlEktefelle.getAdressebeskyttelse())) {
            return new Ektefelle()
                    .withIkketilgangtilektefelle(true);
        }
        return new Ektefelle()
                .withFornavn(finnFornavn(pdlEktefelle.getNavn()))
                .withMellomnavn(finnMellomnavn(pdlEktefelle.getNavn()))
                .withEtternavn(finnEtternavn(pdlEktefelle.getNavn()))
                .withFnr(ektefelleIdent)
                .withFodselsdato(finnFodselsdato(pdlEktefelle.getFoedsel()))
                .withIkketilgangtilektefelle(false)
                .withFolkeregistrertsammen(erFolkeregistrertSammen(pdlPerson.getBostedsadresse(), pdlEktefelle.getBostedsadresse()));
    }

    private String finnFornavn(List<NavnDto> navn) {
        return navn.stream()
                .findFirst().map(NavnDto::getFornavn)
                .orElse("");
    }

    private String finnMellomnavn(List<NavnDto> navn) {
        return navn.stream()
                .findFirst().map(NavnDto::getMellomnavn)
                .orElse("");
    }

    private String finnEtternavn(List<NavnDto> navn) {
        return navn.stream()
                .findFirst().map(NavnDto::getEtternavn)
                .orElse("");
    }

    private LocalDate finnFodselsdato(List<FoedselDto> foedsel) {
        return foedsel.stream().findFirst()
                .map(foedselDto -> new LocalDate(foedselDto.getFoedselsdato().getYear(), foedselDto.getFoedselsdato().getMonthValue(), foedselDto.getFoedselsdato().getDayOfMonth()))
                .orElse(null);
    }

    private boolean erMyndig(List<FoedselDto> foedsel) {
        return finnAlder(foedsel) >= 18;
    }

    private int finnAlder(List<FoedselDto> foedsel) {
        LocalDate foedselsdato = finnFodselsdato(foedsel);
        if (foedselsdato == null) {
            return 0;
        }
        return Years.yearsBetween(foedselsdato, LocalDate.now()).getYears();
    }

    private boolean erDoed(List<FolkeregisterpersonstatusDto> folkeregisterpersonstatus) {
        return folkeregisterpersonstatus.stream().findFirst()
                .map(it -> DOED.equalsIgnoreCase(it.getStatus()))
                .orElse(false);
    }

    private String finnSivilstatus(List<SivilstandDto> sivilstand) {
        return sivilstand.stream().findFirst()
                .map(dto -> dto.getType().name())
                .orElse("");
    }

    private String finnStatsborgerskap(List<StatsborgerskapDto> statsborgerskap) {
        return statsborgerskap.stream().findFirst()
                .map(StatsborgerskapDto::getLand)
                .orElse(NOR);
    }

    private String finnAdressebeskyttelse(List<AdressebeskyttelseDto> adressebeskyttelse) {
        if (adressebeskyttelse == null) {
            return null;
        }
        return adressebeskyttelse.stream()
                .filter(dto -> dto.getGradering() != UGRADERT)
                .findFirst()
                .map(dto -> mapTilDiskresjonskode(dto.getGradering()))
                .orElse(null);
    }

    private boolean harAdressebeskyttelse(List<AdressebeskyttelseDto> adressebeskyttelse) {
        return adressebeskyttelse != null
                && !adressebeskyttelse.isEmpty()
                && !adressebeskyttelse.stream().allMatch(adressebeskyttelseDto -> UGRADERT.equals(adressebeskyttelseDto.getGradering()));
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

    private boolean erFolkeregistrertSammen(List<BostedsadresseDto> personBostedsadresse, List<BostedsadresseDto> barnEllerEktefelleBostedsadresse) {
        BostedsadresseDto bostedsadressePerson = finnBostedsadresse(personBostedsadresse);
        BostedsadresseDto bostedsadresseBarnEllerEktefelle = finnBostedsadresse(barnEllerEktefelleBostedsadresse);
        if (bostedsadressePerson == null && bostedsadresseBarnEllerEktefelle == null) {
            return false;
        }
        return Objects.equals(bostedsadressePerson, bostedsadresseBarnEllerEktefelle);
    }

    private BostedsadresseDto finnBostedsadresse(List<BostedsadresseDto> bostedsadresse) {
        if (bostedsadresse == null || bostedsadresse.isEmpty()) {
            return null;
        }
        return bostedsadresse.stream()
                .filter(dto -> dto.getUkjentBosted() == null && (dto.getVegadresse() != null || dto.getMatrikkeladresse() != null))
                .findFirst()
                .orElse(null);
    }
}
