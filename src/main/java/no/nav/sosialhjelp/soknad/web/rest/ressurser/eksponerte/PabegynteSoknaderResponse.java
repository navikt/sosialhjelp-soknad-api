package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PabegynteSoknaderResponse {

    private final String tittel;

    @JsonCreator
    public PabegynteSoknaderResponse(
            @JsonProperty("tittel") String tittel
    ) {
        this.tittel = tittel;
    }

    public String getTittel() {
        return tittel;
    }
}
