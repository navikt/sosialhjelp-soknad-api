package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class InnsendtSoknadDto {

    private final String tema;
    private final LocalDateTime sisteEndring;
    private final String lenkeInnsyn;

    @JsonCreator
    public InnsendtSoknadDto(
            @JsonProperty("tema") String tema,
            @JsonProperty("sisteEndring") LocalDateTime sisteEndring,
            @JsonProperty("lenkeInnsyn") String lenkeInnsyn
    ) {
        this.tema = tema;
        this.sisteEndring = sisteEndring;
        this.lenkeInnsyn = lenkeInnsyn;
    }

    public String getTema() {
        return tema;
    }

    public LocalDateTime getSisteEndring() {
        return sisteEndring;
    }

    public String getLenkeInnsyn() {
        return lenkeInnsyn;
    }
}
