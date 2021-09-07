package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Oppsummering {

    private final List<Steg> steg;

    public Oppsummering(List<Steg> steg) {
        this.steg = steg;
    }

    public List<Steg> getSteg() {
        return steg;
    }
}
