package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InnsendteSoknaderResponse {

    private final List<InnsendtSoknadDto> innsendteSoknader;

    @JsonCreator
    public InnsendteSoknaderResponse(
            @JsonProperty("innsendteSoknader") List<InnsendtSoknadDto> innsendteSoknader
    ) {
        this.innsendteSoknader = innsendteSoknader;
    }

    public List<InnsendtSoknadDto> getInnsendteSoknader() {
        return innsendteSoknader;
    }
}
