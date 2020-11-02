package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FamilierelasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.StatsborgerskapDto;

import java.util.List;

public class PdlPerson {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    private final List<FamilierelasjonDto> familierelasjoner;

    private final List<NavnDto> navn;

    private final List<SivilstandDto> sivilstand;

    private final List<StatsborgerskapDto> statsborgerskap;

    @JsonCreator
    public PdlPerson(
            @JsonProperty("adressebeskyttelse") List<AdressebeskyttelseDto> adressebeskyttelse,
            @JsonProperty("familierelasjoner") List<FamilierelasjonDto> familierelasjoner,
            @JsonProperty("navn") List<NavnDto> navn,
            @JsonProperty("sivilstand") List<SivilstandDto> sivilstand,
            @JsonProperty("statsborgerskap") List<StatsborgerskapDto> statsborgerskap
    ) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.familierelasjoner = familierelasjoner;
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
