package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InnsendteSoknaderResponse {

    private final String tittel;

    @JsonCreator
    public InnsendteSoknaderResponse(
            @JsonProperty("tittel") String tittel
    ) {
        this.tittel = tittel;
    }

    public String getTittel() {
        return tittel;
    }
}
