package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class InnsendtSoknadDto {

    private final String navn;
    private final String kode;
    private final LocalDateTime sisteEndring;

    @JsonCreator
    public InnsendtSoknadDto(
            @JsonProperty("navn") String navn,
            @JsonProperty("kode") String kode,
            @JsonProperty("sisteEndring") LocalDateTime sisteEndring
    ) {
        this.navn = navn;
        this.kode = kode;
        this.sisteEndring = sisteEndring;
    }

    public String getNavn() {
        return navn;
    }

    public String getKode() {
        return kode;
    }

    public LocalDateTime getSisteEndring() {
        return sisteEndring;
    }

}
