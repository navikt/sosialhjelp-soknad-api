package no.nav.sbl.sosialhjelp.domain;

public enum Vedleggstatus {
    VedleggKreves,
    LastetOpp,
    VedleggAlleredeSendt;

    public boolean er(Vedleggstatus status) {
        return this.equals(status);
    }
}
