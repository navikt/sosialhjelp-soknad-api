package no.nav.sosialhjelp.soknad.domain;

public enum Vedleggstatus {
    VedleggKreves,
    LastetOpp,
    VedleggAlleredeSendt;

    public boolean er(Vedleggstatus status) {
        return this.equals(status);
    }
}
