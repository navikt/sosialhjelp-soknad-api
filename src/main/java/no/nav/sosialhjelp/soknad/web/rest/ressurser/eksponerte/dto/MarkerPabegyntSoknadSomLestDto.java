package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MarkerPabegyntSoknadSomLestDto {

    private final String behandlingsId;
    private final boolean lestDittNav;

    @JsonCreator
    public MarkerPabegyntSoknadSomLestDto(
            @JsonProperty("behandlingsId") String behandlingsId,
            @JsonProperty("lestDittNav") boolean lestDittNav
    ) {
        this.behandlingsId = behandlingsId;
        this.lestDittNav = lestDittNav;
    }

    public String getBehandlingsId() {
        return behandlingsId;
    }

    public boolean isLestDittNav() {
        return lestDittNav;
    }
}
