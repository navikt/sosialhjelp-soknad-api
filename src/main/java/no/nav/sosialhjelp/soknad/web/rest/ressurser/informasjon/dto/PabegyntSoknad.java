package no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon.dto;

import java.time.LocalDateTime;

public class PabegyntSoknad {

    private LocalDateTime sistOppdatert;
    private String behandlingsId;

    public PabegyntSoknad(LocalDateTime sistOppdatert, String behandlingsId) {
        this.sistOppdatert = sistOppdatert;
        this.behandlingsId = behandlingsId;
    }

    public LocalDateTime getSistOppdatert() {
        return sistOppdatert;
    }

    public String getBehandlingsId() {
        return behandlingsId;
    }
}
