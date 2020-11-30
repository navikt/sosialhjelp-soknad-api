package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

}
