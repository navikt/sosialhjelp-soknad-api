package no.nav.sbl.sosialhjelp.domain;

public class Vedleggstatus {
    private Long vedleggstatusId;
    private String eier;
    private Vedleggstatus.Status status;
    private VedleggType vedleggType;
    private Long sendtSoknadId;

    public Long getVedleggstatusId() {
        return vedleggstatusId;
    }

    public Vedleggstatus withVedleggstatusId(Long vedleggstatusId) {
        this.vedleggstatusId = vedleggstatusId;
        return this;
    }

    public String getEier() {
        return eier;
    }

    public Vedleggstatus withEier(String eier) {
        this.eier = eier;
        return this;
    }

    public Vedleggstatus.Status getStatus() {
        return status;
    }

    public Vedleggstatus withStatus(Vedleggstatus.Status status) {
        this.status = status;
        return this;
    }

    public VedleggType getVedleggType() {
        return vedleggType;
    }

    public Vedleggstatus withVedleggType(VedleggType vedleggType) {
        this.vedleggType = vedleggType;
        return this;
    }

    public Long getSendtSoknadId() {
        return sendtSoknadId;
    }

    public Vedleggstatus withSendtSoknadId(Long sendtSoknadId) {
        this.sendtSoknadId = sendtSoknadId;
        return this;
    }

    public enum Status {
        VedleggKreves(0),
        LastetOpp(1),
        VedleggAlleredeSendt(2);

        private int prioritet;

        Status(int prioritet) {
            this.prioritet = prioritet;
        }

        public int getPrioritet() {
            return prioritet;
        }

        public boolean er(Vedleggstatus.Status status) {
            return this.equals(status);
        }

        public boolean erIkke(Vedleggstatus.Status status) {
            return !this.er(status);
        }
    }
}
