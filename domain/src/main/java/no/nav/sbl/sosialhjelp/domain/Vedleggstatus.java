package no.nav.sbl.sosialhjelp.domain;

public class Vedleggstatus {

    public enum Status {
        VedleggKreves,
        LastetOpp,
        VedleggAlleredeSendt;

        public boolean er(Vedleggstatus.Status status) {
            return this.equals(status);
        }
    }
}
