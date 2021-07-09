package no.nav.sosialhjelp.soknad.business.service.adressesok;

public enum Soketype {
    FONETISK("F"),
    EKSAKT("E"),
    TILFELDIG("T"),
    LIGNENDE("L");

    String tpsKode;

    Soketype(String tpsKode) {
        this.tpsKode = tpsKode;
    }

    public String toTpsKode() {
        return tpsKode;
    }
}
