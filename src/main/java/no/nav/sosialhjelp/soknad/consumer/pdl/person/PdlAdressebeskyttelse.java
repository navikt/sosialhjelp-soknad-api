package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;

import java.util.List;

public class PdlAdressebeskyttelse {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    @JsonCreator
    public PdlAdressebeskyttelse(@JsonProperty("adressebeskyttelse") List<AdressebeskyttelseDto> adressebeskyttelse) {
        this.adressebeskyttelse = adressebeskyttelse;
    }

    public List<AdressebeskyttelseDto> getAdressebeskyttelse() {
        return adressebeskyttelse;
    }
}
