package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

public class MarkerPabegyntSoknadSomLestDto {

    private final String behandlingsId;
    private final boolean lestDittNav;

    public MarkerPabegyntSoknadSomLestDto(String behandlingsId, boolean lestDittNav) {
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
