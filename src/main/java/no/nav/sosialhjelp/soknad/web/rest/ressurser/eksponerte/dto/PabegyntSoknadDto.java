package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class PabegyntSoknadDto {

    private final LocalDateTime tidspunkt;
    private final String grupperingsId;
    private final String tekst;
    private final String link;
    private final Integer sikkerhetsnivaa;
    private final LocalDateTime synligFremTil;
    private final boolean eksternVarsling;

    @JsonCreator
    public PabegyntSoknadDto(
            @JsonProperty("tidspunkt") LocalDateTime tidspunkt,
            @JsonProperty("grupperingsId") String grupperingsId,
            @JsonProperty("tekst") String tekst,
            @JsonProperty("link") String link,
            @JsonProperty("sikkerhetsnivaa") Integer sikkerhetsnivaa,
            @JsonProperty("synligFremTil") LocalDateTime synligFremTil,
            @JsonProperty("eksternVarsling") boolean eksternVarsling
    ) {
        this.tidspunkt = tidspunkt;
        this.grupperingsId = grupperingsId;
        this.tekst = tekst;
        this.link = link;
        this.sikkerhetsnivaa = sikkerhetsnivaa;
        this.synligFremTil = synligFremTil;
        this.eksternVarsling = eksternVarsling;
    }

    public LocalDateTime getTidspunkt() {
        return tidspunkt;
    }

    public String getGrupperingsId() {
        return grupperingsId;
    }

    public String getTekst() {
        return tekst;
    }

    public String getLink() {
        return link;
    }

    public Integer getSikkerhetsnivaa() {
        return sikkerhetsnivaa;
    }

    public LocalDateTime getSynligFremTil() {
        return synligFremTil;
    }

    public boolean isEksternVarsling() {
        return eksternVarsling;
    }
}
