package no.nav.sosialhjelp.soknad.oppslag.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KontonummerDto {

    private final String kontonummer;

    @JsonCreator
    public KontonummerDto(
            @JsonProperty("kontonummer") String kontonummer
    ) {
        this.kontonummer = kontonummer;
    }

    public String getKontonummer() {
        return kontonummer;
    }
}
