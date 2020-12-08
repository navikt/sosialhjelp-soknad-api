package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BostedsadresseDto {

    private final String coAdressenavn;
    private final VegadresseDto vegadresse;
    private final MatrikkeladresseDto matrikkeladresse;
    private final UkjentBostedDto ukjentBosted;

    @JsonCreator
    public BostedsadresseDto(
            @JsonProperty("coAdressenavn") String coAdressenavn,
            @JsonProperty("vegadresse") VegadresseDto vegadresse,
            @JsonProperty("matrikkeladresse") MatrikkeladresseDto matrikkeladresse,
            @JsonProperty("ukjentBosted") UkjentBostedDto ukjentBosted
    ) {
        this.coAdressenavn = coAdressenavn;
        this.vegadresse = vegadresse;
        this.matrikkeladresse = matrikkeladresse;
        this.ukjentBosted = ukjentBosted;
    }

    public String getCoAdressenavn() {
        return coAdressenavn;
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
