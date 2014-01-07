package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
    private String gosysId;
    private Integer antallSider;
    private byte[] data;
    private String fillagerReferanse;

    public Vedlegg() {
    }

    public Vedlegg(Long vedleggId, Long soknadId, Long faktumId, String gosysId, String navn, Long storrelse, Integer antallSider, String fillagerReferanse, byte[] data) {
        this.id = vedleggId;
        this.soknadId = soknadId;
        this.faktumId = faktumId;
        this.gosysId = gosysId;
        this.navn = navn;
        this.storrelse = storrelse;
        this.data = data;
        this.antallSider = antallSider;
        this.fillagerReferanse = fillagerReferanse;
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

    public String getGosysId() {
        return gosysId;
    }

    public void setGosysId(String gosysId) {
        this.gosysId = gosysId;
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Vedlegg rhs = (Vedlegg) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.soknadId, rhs.soknadId)
                .append(this.navn, rhs.navn)
                .append(this.storrelse, rhs.storrelse)
                .append(this.faktumId, rhs.faktumId)
                .append(this.gosysId, rhs.gosysId)
                .append(this.antallSider, rhs.antallSider)
                .append(this.data, rhs.data)
                .append(this.fillagerReferanse, rhs.fillagerReferanse)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(soknadId)
                .append(navn)
                .append(storrelse)
                .append(faktumId)
                .append(gosysId)
                .append(antallSider)
                .append(data)
                .append(fillagerReferanse)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("soknadId", soknadId)
                .append("navn", navn)
                .append("storrelse", storrelse)
                .append("faktumId", faktumId)
                .append("gosysId", gosysId)
                .append("antallSider", antallSider)
                .append("data", data)
                .append("fillagerReferanse", fillagerReferanse)
                .toString();
    }
}
