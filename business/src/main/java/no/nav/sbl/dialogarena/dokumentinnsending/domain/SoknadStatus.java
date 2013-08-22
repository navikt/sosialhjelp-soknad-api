package no.nav.sbl.dialogarena.dokumentinnsending.domain;

public enum SoknadStatus {
    UNDER_ARBEID,
    FERDIG,
    AVBRUTT_AV_BRUKER,
    IKKE_SPESIFISERT;

    public boolean er(SoknadStatus status) {
        return this.equals(status);
    }

    public boolean erIkke(SoknadStatus status) {
        return !this.er(status);
    }
}
