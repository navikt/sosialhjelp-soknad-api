package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.io.InputStream;

/**
 * Domeneklasse som beskriver et vedlegg.
 */
public class Vedlegg {
    private Long id;
    private Long soknadId;
    private String navn;
    private Long storrelse;
    private Long faktum;
    private InputStream inputStream;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public Long getFaktum() {
        return faktum;
    }

    public void setFaktum(Long faktum) {
        this.faktum = faktum;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public Long getStorrelse() {
        return storrelse;
    }

    public void setStorrelse(long storrelse) {
        this.storrelse = storrelse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vedlegg vedlegg = (Vedlegg) o;

        if (faktum != null ? !faktum.equals(vedlegg.faktum) : vedlegg.faktum != null) return false;
        if (id != null ? !id.equals(vedlegg.id) : vedlegg.id != null) return false;
        if (navn != null ? !navn.equals(vedlegg.navn) : vedlegg.navn != null) return false;
        if (soknadId != null ? !soknadId.equals(vedlegg.soknadId) : vedlegg.soknadId != null) return false;
        if (storrelse != null ? !storrelse.equals(vedlegg.storrelse) : vedlegg.storrelse != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (soknadId != null ? soknadId.hashCode() : 0);
        result = 31 * result + (navn != null ? navn.hashCode() : 0);
        result = 31 * result + (storrelse != null ? storrelse.hashCode() : 0);
        result = 31 * result + (faktum != null ? faktum.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vedlegg{");
        sb.append("id=").append(id);
        sb.append(", soknadId=").append(soknadId);
        sb.append(", navn='").append(navn).append('\'');
        sb.append(", storrelse=").append(storrelse);
        sb.append(", faktum='").append(faktum).append('\'');
        sb.append(", inputStream=").append(inputStream);
        sb.append('}');
        return sb.toString();
    }
}
