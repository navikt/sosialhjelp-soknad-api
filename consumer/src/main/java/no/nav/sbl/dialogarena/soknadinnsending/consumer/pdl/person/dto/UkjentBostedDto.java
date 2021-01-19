package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UkjentBostedDto {

    private final String bostedskommune;

    @JsonCreator
    public UkjentBostedDto(
            @JsonProperty("bostedskommune") String bostedskommune
    ) {
        this.bostedskommune = bostedskommune;
    }

    public String getBostedskommune() {
        return bostedskommune;
    }

}
