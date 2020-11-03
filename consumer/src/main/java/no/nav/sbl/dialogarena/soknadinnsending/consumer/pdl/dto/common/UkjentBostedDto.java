package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UkjentBostedDto that = (UkjentBostedDto) o;
        return Objects.equals(bostedskommune, that.bostedskommune);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bostedskommune);
    }
}
