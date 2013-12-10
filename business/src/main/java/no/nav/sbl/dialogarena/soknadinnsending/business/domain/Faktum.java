package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.io.Serializable;

public class Faktum implements Serializable {
    public enum Status {
        IkkeVedlegg,
        VedleggKreves,
        LastetOpp,
        SendesSenere,
        SendesIkke;

        public boolean er(Status status) {
            return this.equals(status);
        }
    }

    //public enum FaktumType { FAGREGISTER, BRUKERREGISTRERT; }
    private Long faktumId;
    private Long soknadId;
    private Long vedleggId;
    private Status innsendingsvalg;
    private String key;
    private String value;
    private String type;

    public Faktum() {
    }

    public Faktum(Long soknadId, Long faktumId, String key, String value, String type) {
        this(soknadId, faktumId, key, value);
        this.type = type;
    }

    public Faktum(Long soknadId, Long faktumId, String key, String value) {
        this.soknadId = soknadId;
        this.faktumId = faktumId;
        this.key = key;
        this.value = value;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public void setFaktumId(Long faktumId) {
        this.faktumId = faktumId;
    }

    public Long getVedleggId() {
        return vedleggId;
    }

    public void setVedleggId(Long vedleggId) {
        this.vedleggId = vedleggId;
    }

    public String getValue() {
        return value;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public final void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Status getInnsendingsvalg() {
        if (innsendingsvalg == null) {
            innsendingsvalg = Status.IkkeVedlegg;
        }
        return innsendingsvalg;
    }

    public void setInnsendingsvalg(Status innsendingsvalg) {
        this.innsendingsvalg = innsendingsvalg;
    }

    @Override
    public String toString() {
        return "Faktum [soknadId=" + soknadId + ", key=" + key + ", value="
                + value + ", type=" + type + "]";
    }

}