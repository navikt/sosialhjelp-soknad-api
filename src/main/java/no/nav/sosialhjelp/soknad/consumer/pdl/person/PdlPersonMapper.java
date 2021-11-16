package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.google.common.collect.ImmutableMap;
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto.Gradering;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.BostedsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FoedselDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FolkeregisterpersonstatusDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.KontaktadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.MatrikkeladresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.NavnDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.OppholdsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.StatsborgerskapDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.VegadresseDto;
import no.nav.sosialhjelp.soknad.domain.model.Barn;
import no.nav.sosialhjelp.soknad.domain.model.Ektefelle;
import no.nav.sosialhjelp.soknad.person.domain.Bostedsadresse;
import no.nav.sosialhjelp.soknad.person.domain.Kontaktadresse;
import no.nav.sosialhjelp.soknad.person.domain.Matrikkeladresse;
import no.nav.sosialhjelp.soknad.person.domain.Oppholdsadresse;
import no.nav.sosialhjelp.soknad.person.domain.Person;
import no.nav.sosialhjelp.soknad.person.domain.Vegadresse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto.Gradering.UGRADERT;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.ENKE_ELLER_ENKEMANN;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.GIFT;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.GJENLEVENDE_PARTNER;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.REGISTRERT_PARTNER;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.SEPARERT;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.SEPARERT_PARTNER;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.SKILT;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.SKILT_PARTNER;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.UGIFT;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.UOPPGITT;

@Component
public class PdlPersonMapper {

    static final String NOR = "NOR";
    static final String DOED = "DOED";

    private static final Map<SivilstandDto.SivilstandType, String> MAP_PDLSIVILSTAND_TIL_JSONSIVILSTATUS = new ImmutableMap.Builder<SivilstandDto.SivilstandType, String>()
            .put(UOPPGITT, "")
            .put(UGIFT, "ugift")
            .put(GIFT, "gift")
            .put(ENKE_ELLER_ENKEMANN, "enke")
            .put(SKILT, "skilt")
            .put(SEPARERT, "separert")
            .put(REGISTRERT_PARTNER, "gift")
            .put(SEPARERT_PARTNER, "separert")
            .put(SKILT_PARTNER, "skilt")
            .put(GJENLEVENDE_PARTNER, "enke")
            .build();

    private final PdlPersonMapperHelper helper = new PdlPersonMapperHelper();

    private final KodeverkService kodeverkService;

    public PdlPersonMapper(KodeverkService kodeverkService) {
        this.kodeverkService = kodeverkService;
    }

    public Person mapToPerson(PdlPerson pdlPerson, String ident) {
        if (pdlPerson == null) {
            return null;
        }
        return new Person(
                findFornavn(pdlPerson.getNavn()),
                findMellomnavn(pdlPerson.getNavn()),
                findEtternavn(pdlPerson.getNavn()),
                ident,
                findSivilstatus(pdlPerson.getSivilstand()),
                findStatsborgerskap(pdlPerson.getStatsborgerskap()),
                null,
                mapToBostedsadresse(pdlPerson.getBostedsadresse()),
                mapToOppholdssadresse(pdlPerson.getOppholdsadresse(), pdlPerson.getBostedsadresse()),
                maptoKontaktadresse(pdlPerson.getKontaktadresse(), pdlPerson.getBostedsadresse())
        );
    }

    public Barn mapToBarn(PdlBarn pdlBarn, String barnIdent, PdlPerson pdlPerson) {
        if (hasAdressebeskyttelse(pdlBarn.getAdressebeskyttelse())) {
            return null;
        }
        if (isMyndig(pdlBarn.getFoedsel()) || isDoed(pdlBarn.getFolkeregisterpersonstatus())) {
            return null;
        }
        return new Barn()
                .withFornavn(findFornavn(pdlBarn.getNavn()))
                .withMellomnavn(findMellomnavn(pdlBarn.getNavn()))
                .withEtternavn(findEtternavn(pdlBarn.getNavn()))
                .withFnr(barnIdent)
                .withFodselsdato(findFodselsdato(pdlBarn.getFoedsel()))
                .withFolkeregistrertsammen(isFolkeregistrertSammen(pdlPerson.getBostedsadresse(), pdlBarn.getBostedsadresse()));
    }

    public Ektefelle mapToEktefelle(PdlEktefelle pdlEktefelle, String ektefelleIdent, PdlPerson pdlPerson) {
        if (pdlEktefelle == null) {
            return null;
        }
        if (hasAdressebeskyttelse(pdlEktefelle.getAdressebeskyttelse())) {
            return new Ektefelle()
                    .withIkketilgangtilektefelle(true);
        }
        return new Ektefelle()
                .withFornavn(findFornavn(pdlEktefelle.getNavn()))
                .withMellomnavn(findMellomnavn(pdlEktefelle.getNavn()))
                .withEtternavn(findEtternavn(pdlEktefelle.getNavn()))
                .withFnr(ektefelleIdent)
                .withFodselsdato(findFodselsdato(pdlEktefelle.getFoedsel()))
                .withIkketilgangtilektefelle(false)
                .withFolkeregistrertsammen(isFolkeregistrertSammen(pdlPerson.getBostedsadresse(), pdlEktefelle.getBostedsadresse()));
    }

    public Gradering mapToAdressebeskyttelse(PdlAdressebeskyttelse pdlAdressebeskyttelse) {
        if (pdlAdressebeskyttelse == null) {
            return null;
        }
        return pdlAdressebeskyttelse.getAdressebeskyttelse().stream()
                .findFirst()
                .map(AdressebeskyttelseDto::getGradering)
                .orElse(null);
    }

    private String findFornavn(List<NavnDto> navn) {
        return Optional.ofNullable(helper.utledGjeldendeNavn(navn))
                .map(NavnDto::getFornavn).orElse("").toUpperCase();
    }

    private String findMellomnavn(List<NavnDto> navn) {
        return Optional.ofNullable(helper.utledGjeldendeNavn(navn))
                .map(NavnDto::getMellomnavn).orElse("").toUpperCase();
    }

    private String findEtternavn(List<NavnDto> navn) {
        return Optional.ofNullable(helper.utledGjeldendeNavn(navn))
                .map(NavnDto::getEtternavn).orElse("").toUpperCase();
    }

    private LocalDate findFodselsdato(List<FoedselDto> foedsel) {
        return foedsel.stream().findFirst()
                .map(foedselDto -> LocalDate.of(foedselDto.getFoedselsdato().getYear(), foedselDto.getFoedselsdato().getMonthValue(), foedselDto.getFoedselsdato().getDayOfMonth()))
                .orElse(null);
    }

    private boolean isMyndig(List<FoedselDto> foedsel) {
        return findAlder(foedsel) >= 18;
    }

    private int findAlder(List<FoedselDto> foedsel) {
        LocalDate foedselsdato = findFodselsdato(foedsel);
        if (foedselsdato == null) {
            return 0;
        }
        return Period.between(foedselsdato, LocalDate.now()).getYears();
    }

    private boolean isDoed(List<FolkeregisterpersonstatusDto> folkeregisterpersonstatus) {
        return folkeregisterpersonstatus.stream().findFirst()
                .map(it -> DOED.equalsIgnoreCase(it.getStatus()))
                .orElse(false);
    }

    private String findSivilstatus(List<SivilstandDto> sivilstand) {
        var sivilstandDto = helper.utledGjeldendeSivilstand(sivilstand);
        return sivilstandDto != null ? MAP_PDLSIVILSTAND_TIL_JSONSIVILSTATUS.get(sivilstandDto.getType()) : "";
    }

    private List<String> findStatsborgerskap(List<StatsborgerskapDto> statsborgerskap) {
        var list = statsborgerskap.stream()
                .map(StatsborgerskapDto::getLand)
                .collect(Collectors.toList());
        return list.isEmpty() ? List.of(NOR) : list;
    }

    private boolean hasAdressebeskyttelse(List<AdressebeskyttelseDto> adressebeskyttelse) {
        return adressebeskyttelse != null
                && !adressebeskyttelse.isEmpty()
                && !adressebeskyttelse.stream().allMatch(adressebeskyttelseDto -> UGRADERT.equals(adressebeskyttelseDto.getGradering()));
    }

    private boolean isFolkeregistrertSammen(List<BostedsadresseDto> personBostedsadresse, List<BostedsadresseDto> barnEllerEktefelleBostedsadresse) {
        BostedsadresseDto bostedsadressePerson = findBostedsadresse(personBostedsadresse);
        BostedsadresseDto bostedsadresseBarnEllerEktefelle = findBostedsadresse(barnEllerEktefelleBostedsadresse);
        if (bostedsadressePerson == null && bostedsadresseBarnEllerEktefelle == null) {
            return false;
        }
        // Hvis person og barnEllerEktefelle har bostedsadresse med lik matrikkelId - betyr det at de er registrert som bosatt på samme adresse.
        Optional<String> matrikkelIdPerson = getMatrikkelId(bostedsadressePerson);
        Optional<String> matrikkelIdBarnEllerEktefelle = getMatrikkelId(bostedsadresseBarnEllerEktefelle);
        if (matrikkelIdPerson.isPresent() && matrikkelIdBarnEllerEktefelle.isPresent()) {
            return matrikkelIdPerson.get().equals(matrikkelIdBarnEllerEktefelle.get());
        }
        // Hvis ikke vegadresse til person eller barnEllerEktefelle har matrikkelId, sammenlign resterende vegadresse-felter
        if (bostedsadressePerson != null && bostedsadressePerson.getVegadresse() != null &&
                bostedsadresseBarnEllerEktefelle != null && bostedsadresseBarnEllerEktefelle.getVegadresse() != null) {
            return isEqualVegadresser(bostedsadressePerson.getVegadresse(), bostedsadresseBarnEllerEktefelle.getVegadresse());
        }

        return false;
    }

    private BostedsadresseDto findBostedsadresse(List<BostedsadresseDto> bostedsadresse) {
        if (bostedsadresse == null || bostedsadresse.isEmpty()) {
            return null;
        }
        return bostedsadresse.stream()
                .filter(dto -> dto.getUkjentBosted() == null && (dto.getVegadresse() != null || dto.getMatrikkeladresse() != null))
                .findFirst()
                .orElse(null);
    }

    private Optional<String> getMatrikkelId(BostedsadresseDto bostedsadresseDto) {
        if (hasVegadresseMatrikkelId(bostedsadresseDto)) {
            return Optional.of(bostedsadresseDto.getVegadresse().getMatrikkelId());
        }
        if (hasMatrikkeladresseMatrikkelId(bostedsadresseDto)) {
            return Optional.of(bostedsadresseDto.getMatrikkeladresse().getMatrikkelId());
        }
        return Optional.empty();
    }

    private boolean hasVegadresseMatrikkelId(BostedsadresseDto bostedsadresseDto) {
        return bostedsadresseDto != null && bostedsadresseDto.getVegadresse() != null && bostedsadresseDto.getVegadresse().getMatrikkelId() != null;
    }

    private boolean hasMatrikkeladresseMatrikkelId(BostedsadresseDto bostedsadresseDto) {
        return bostedsadresseDto != null && bostedsadresseDto.getMatrikkeladresse() != null && bostedsadresseDto.getMatrikkeladresse().getMatrikkelId() != null;
    }

    private boolean isEqualVegadresser(VegadresseDto adr1, VegadresseDto adr2) {
        return Objects.equals(adr1.getAdressenavn(), adr2.getAdressenavn())
                && Objects.equals(adr1.getHusnummer(), adr2.getHusnummer())
                && Objects.equals(adr1.getHusbokstav(), adr2.getHusbokstav())
                && Objects.equals(adr1.getTilleggsnavn(), adr2.getTilleggsnavn())
                && Objects.equals(adr1.getPostnummer(), adr2.getPostnummer())
                && Objects.equals(adr1.getKommunenummer(), adr2.getKommunenummer())
                && Objects.equals(adr1.getBruksenhetsnummer(), adr2.getBruksenhetsnummer())
                && Objects.equals(adr1.getBydelsnummer(), adr2.getBydelsnummer());
    }

    private boolean isEqualVegadresserWithoutKommunenummer(VegadresseDto adr1, VegadresseDto adr2) {
        return Objects.equals(adr1.getAdressenavn(), adr2.getAdressenavn())
                && Objects.equals(adr1.getHusnummer(), adr2.getHusnummer())
                && Objects.equals(adr1.getHusbokstav(), adr2.getHusbokstav())
                && Objects.equals(adr1.getTilleggsnavn(), adr2.getTilleggsnavn())
                && Objects.equals(adr1.getPostnummer(), adr2.getPostnummer())
                && Objects.equals(adr1.getBruksenhetsnummer(), adr2.getBruksenhetsnummer());
    }

    private Bostedsadresse mapToBostedsadresse(List<BostedsadresseDto> dtos) {
        var dto = findBostedsadresse(dtos);
        if (dto == null) {
            return null;
        }
        return new Bostedsadresse(
                dto.getCoAdressenavn(),
                dto.getVegadresse() == null ? null : mapToVegadresse(dto.getVegadresse()),
                dto.getMatrikkeladresse() == null ? null : mapToMatrikkeladresse(dto.getMatrikkeladresse())
        );
    }

    private Oppholdsadresse mapToOppholdssadresse(List<OppholdsadresseDto> dtos, List<BostedsadresseDto> bostedsadresseDtos) {
        if (dtos == null || dtos.isEmpty()) {
            return null;
        }
        // Todo: vi er kun interessert i norske oppholdsadresser med en faktisk adresse.
        //  Fra doc:
        //  Man kan ha en oppholdsadresse med Freg som master og en med PDL som master.
        //  Flertallet av oppholdsadressene fra Freg vil være norske, og flertallet av oppholdsadresser registrert av NAV vil være utenlandske.
        //  Fra folkeregisteret kan man også få oppholdsadresse uten en faktisk adresse, men med informasjon i oppholdAnnetSted.
        return dtos.stream()
                .filter(dto -> dto.getVegadresse() != null)
                .filter(dto -> filterVegadresseNotEqualToBostedsadresse(bostedsadresseDtos, dto.getVegadresse()))
                .findFirst()
                .map(it -> new Oppholdsadresse(it.getCoAdressenavn(), mapToVegadresse(it.getVegadresse())))
                .orElse(null);
    }

    private Kontaktadresse maptoKontaktadresse(List<KontaktadresseDto> dtos, List<BostedsadresseDto> bostedsadresseDtos) {
        if (dtos == null || dtos.isEmpty()) {
            return null;
        }
        return dtos.stream()
                .filter(dto -> dto.getVegadresse() != null)
                .filter(dto -> filterVegadresseNotEqualToBostedsadresse(bostedsadresseDtos, dto.getVegadresse()))
                .findFirst()
                .map(it -> new Kontaktadresse(it.getCoAdressenavn(), mapToVegadresse(it.getVegadresse())))
                .orElse(null);
    }

    private boolean filterVegadresseNotEqualToBostedsadresse(List<BostedsadresseDto> bostedsadresseDtos, VegadresseDto dtoVegadresse) {
        var bostedsadresseDto = findBostedsadresse(bostedsadresseDtos);
        if (bostedsadresseDto != null && bostedsadresseDto.getVegadresse() != null) {
            if (dtoVegadresse.getKommunenummer() != null) {
                return !isEqualVegadresser(dtoVegadresse, bostedsadresseDto.getVegadresse());
            }
            return !isEqualVegadresserWithoutKommunenummer(dtoVegadresse, bostedsadresseDto.getVegadresse());
        }
        return false;
    }

    private Vegadresse mapToVegadresse(VegadresseDto dto) {
        return new Vegadresse(
                dto.getAdressenavn().toUpperCase(),
                dto.getHusnummer(),
                dto.getHusbokstav(),
                dto.getTilleggsnavn(),
                dto.getPostnummer(),
                getPoststed(dto.getPostnummer()),
                dto.getKommunenummer(),
                dto.getBruksenhetsnummer(),
                dto.getBydelsnummer()
        );
    }

    private String getPoststed(String postnummer) {
        var poststed = kodeverkService.getPoststed(postnummer);
        if (poststed != null) {
            return poststed.toUpperCase();
        }
        return null;
    }

    private Matrikkeladresse mapToMatrikkeladresse(MatrikkeladresseDto dto) {
        return new Matrikkeladresse(
                dto.getMatrikkelId(),
                dto.getPostnummer(),
                getPoststed(dto.getPostnummer()),
                dto.getTilleggsnavn(),
                dto.getKommunenummer(),
                dto.getBruksenhetsnummer()
        );
    }
}
