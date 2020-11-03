package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class BostedsadresseDto {

    private final VegadresseDto vegadresse;
    private final MatrikkeladresseDto matrikkeladresse;
    private final UkjentBostedDto ukjentBosted;

    @JsonCreator
    public BostedsadresseDto(
            @JsonProperty("vegadresse") VegadresseDto vegadresse,
            @JsonProperty("matrikkeladresse") MatrikkeladresseDto matrikkeladresse,
            @JsonProperty("ukjentBosted") UkjentBostedDto ukjentBosted) {
        this.vegadresse = vegadresse;
        this.matrikkeladresse = matrikkeladresse;
        this.ukjentBosted = ukjentBosted;
    }

    public VegadresseDto getVegadresse() {
        return vegadresse;
    }

    public MatrikkeladresseDto getMatrikkeladresse() {
        return matrikkeladresse;
    }

    public UkjentBostedDto getUkjentBosted() {
        return ukjentBosted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BostedsadresseDto that = (BostedsadresseDto) o;
        return Objects.equals(vegadresse, that.vegadresse) &&
                Objects.equals(matrikkeladresse, that.matrikkeladresse) &&
                Objects.equals(ukjentBosted, that.ukjentBosted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vegadresse, matrikkeladresse, ukjentBosted);
    }
}
