package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.BostedsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FoedselDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FolkeregisterpersonstatusDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.NavnDto;

import java.util.List;

public class PdlBarn {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    private final List<BostedsadresseDto> bostedsadresse;

    private final List<FolkeregisterpersonstatusDto> folkeregisterpersonstatus;

    private final List<FoedselDto> foedsel;

    private final List<NavnDto> navn;

    @JsonCreator
    public PdlBarn(
            @JsonProperty("adressebeskyttelse") List<AdressebeskyttelseDto> adressebeskyttelse,
            @JsonProperty("bostedsadresse") List<BostedsadresseDto> bostedsadresse,
            @JsonProperty("folkeregisterpersonstatus") List<FolkeregisterpersonstatusDto> folkeregisterpersonstatus,
            @JsonProperty("foedsel") List<FoedselDto> foedsel,
            @JsonProperty("navn") List<NavnDto> navn
    ) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.bostedsadresse = bostedsadresse;
        this.folkeregisterpersonstatus = folkeregisterpersonstatus;
        this.foedsel = foedsel;
        this.navn = navn;
    }

    public List<AdressebeskyttelseDto> getAdressebeskyttelse() {
        return adressebeskyttelse;
    }

    public List<BostedsadresseDto> getBostedsadresse() {
        return bostedsadresse;
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

}
