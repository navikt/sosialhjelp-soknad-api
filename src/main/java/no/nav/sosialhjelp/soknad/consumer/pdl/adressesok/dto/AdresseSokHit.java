package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdresseSokHit {

    private final AdresseDto vegadresse;
    private final Float score;

    @JsonCreator
    public AdresseSokHit(
            @JsonProperty("vegadresse") AdresseDto vegadresse,
            @JsonProperty("score") Float score
    ) {
        this.vegadresse = vegadresse;
        this.score = score;
    }

    public AdresseDto getVegadresse() {
        return vegadresse;
    }

    public Float getScore() {
        return score;
    }
}
