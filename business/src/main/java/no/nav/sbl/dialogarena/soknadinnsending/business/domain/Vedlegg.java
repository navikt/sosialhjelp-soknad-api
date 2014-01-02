package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Domeneklasse som beskriver et vedlegg.
 */
public class Vedlegg {
    private Long id;
    private Long soknadId;
    private String navn;
    private Long storrelse;
    private Long faktumId;
    private Integer antallSider;
    private byte[] data;
    private String fillagerReferanse;

    public Vedlegg() {
    }

    public Vedlegg(Long vedleggId, Long soknadId, Long faktumId, String navn, Long storrelse, Integer antallSider, byte[] data) {
        this.id = vedleggId;
        this.soknadId = soknadId;
        this.faktumId = faktumId;
        this.navn = navn;
        this.storrelse = storrelse;
        this.data = data;
        this.antallSider = antallSider;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public String getNavn() {
        return navn;
    }

    public Long getStorrelse() {
        return storrelse;
    }

    public Integer getAntallSider() {
        return antallSider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vedlegg vedlegg = (Vedlegg) o;

        if (tomtFaktum(vedlegg)) {
            return false;
        }
        if (tomId(vedlegg)) {
            return false;
        }
        if (tomtNavn(vedlegg)) {
            return false;
        }
        if (tomSoknadId(vedlegg)) {
            return false;
        }
        if (tomStorrelse(vedlegg)) {
            return false;
        }

        return true;
    }

    private boolean tomStorrelse(Vedlegg vedlegg) {
        if (storrelse != null) {
            return !storrelse.equals(vedlegg.storrelse);
        }
        return vedlegg.storrelse != null;
    }

    private boolean tomSoknadId(Vedlegg vedlegg) {
        if (soknadId != null) {
            return !soknadId.equals(vedlegg.soknadId);
        }
        return vedlegg.soknadId != null;
    }

    private boolean tomtNavn(Vedlegg vedlegg) {
        if (navn != null) {
            return !navn.equals(vedlegg.navn);
        }
        return vedlegg.navn != null;
    }

    private boolean tomId(Vedlegg vedlegg) {
        if (id != null) {
            return !id.equals(vedlegg.id);
        }
        return vedlegg.id != null;
    }

    private boolean tomtFaktum(Vedlegg vedlegg) {
        if (faktumId != null) {
            return !faktumId.equals(vedlegg.faktumId);
        }
        return vedlegg.faktumId != null;
    }

    public String getFillagerReferanse() {
        return fillagerReferanse;
    }

    public void setFillagerReferanse(String fillagerReferanse) {
        this.fillagerReferanse = fillagerReferanse;
    }

    @XmlTransient
    @JsonIgnore
    public byte[] getData() {
        return data;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (soknadId != null ? soknadId.hashCode() : 0);
        result = 31 * result + (navn != null ? navn.hashCode() : 0);
        result = 31 * result + (storrelse != null ? storrelse.hashCode() : 0);
        result = 31 * result + (faktumId != null ? faktumId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vedlegg{");
        sb.append("id=").append(id);
        sb.append(", soknadId=").append(soknadId);
        sb.append(", navn='").append(navn).append('\'');
        sb.append(", storrelse=").append(storrelse);
        sb.append(", faktumId='").append(faktumId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
