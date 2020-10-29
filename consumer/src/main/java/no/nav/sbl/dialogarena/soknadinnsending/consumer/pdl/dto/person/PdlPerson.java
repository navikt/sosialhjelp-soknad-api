package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PdlPerson {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    private final List<FamilierelasjonDto> familierelasjoner;

    private final List<FolkeregisterpersonstatusDto> folkeregisterpersonstatus;

    private final List<FoedselDto> foedsel;

    private final List<NavnDto> navn;

    private final List<SivilstandDto> sivilstand;

    private final List<StatsborgerskapDto> statsborgerskap;

    @JsonCreator
    public PdlPerson(
            @JsonProperty("adressebeskyttelse") List<AdressebeskyttelseDto> adressebeskyttelse,
            @JsonProperty("familierelasjoner") List<FamilierelasjonDto> familierelasjoner,
            @JsonProperty("folkeregisterpersonstatus") List<FolkeregisterpersonstatusDto> folkeregisterpersonstatus,
            @JsonProperty("foedsel") List<FoedselDto> foedsel,
            @JsonProperty("navn") List<NavnDto> navn,
            @JsonProperty("sivilstand") List<SivilstandDto> sivilstand,
            @JsonProperty("statsborgerskap") List<StatsborgerskapDto> statsborgerskap
    ) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.familierelasjoner = familierelasjoner;
        this.folkeregisterpersonstatus = folkeregisterpersonstatus;
        this.foedsel = foedsel;
        this.navn = navn;
        this.sivilstand = sivilstand;
        this.statsborgerskap = statsborgerskap;
    }

    public List<AdressebeskyttelseDto> getAdressebeskyttelse() {
        return adressebeskyttelse;
    }

    public List<FamilierelasjonDto> getFamilierelasjoner() {
        return familierelasjoner;
    }

    public List<FolkeregisterpersonstatusDto> getFolkeregisterpersonstatus() {
        return folkeregisterpersonstatus;
    }

    public List<FoedselDto> getFoedsel() {
        return foedsel;
    }

    public List<NavnDto> getNavn() {
        return navn;
    }

    public List<SivilstandDto> getSivilstand() {
        return sivilstand;
    }

    public List<StatsborgerskapDto> getStatsborgerskap() {
        return statsborgerskap;
    }
}
