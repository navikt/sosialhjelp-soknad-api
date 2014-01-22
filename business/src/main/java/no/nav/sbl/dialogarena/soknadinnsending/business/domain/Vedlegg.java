package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domeneklasse som beskriver et vedlegg.
 */
public class Vedlegg {
    private Long vedleggId;
    private Long soknadId;
    private Long faktumId;
    private String skjemaNummer;
    private Status innsendingsvalg;
    private String beskrivelse;
    private String navn = "";
    private Long storrelse = 0L;
    private Integer antallSider = 0;
    private byte[] data;
    private String fillagerReferanse = UUID.randomUUID().toString();
    private Map<String, String> urls = new HashMap<>();
    private String tittel;

    public Vedlegg() {
    }

    public Vedlegg(Long soknadId, Long faktumId, String skjemaNummer, Status innsendingsvalg) {
        this.soknadId = soknadId;
        this.faktumId = faktumId;
        this.skjemaNummer = skjemaNummer;
        this.innsendingsvalg = innsendingsvalg;
    }

    public Vedlegg(Long vedleggId, Long soknadId, Long faktumId, String skjemaNummer, String navn, Long storrelse, Integer antallSider, String fillagerReferanse, byte[] data, Status innsendingsvalg) {
        this.vedleggId = vedleggId;
        this.soknadId = soknadId;
        this.faktumId = faktumId;
        this.skjemaNummer = skjemaNummer;
        this.navn = navn;
        this.beskrivelse = navn;
        this.storrelse = storrelse;
        this.data = data;
        this.antallSider = antallSider;
        this.fillagerReferanse = fillagerReferanse;
        this.innsendingsvalg = innsendingsvalg;
    }

    public Long getVedleggId() {
        return vedleggId;
    }

    public void setVedleggId(Long id) {
        this.vedleggId = id;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public Status getInnsendingsvalg() {
        return innsendingsvalg;
    }

    public void setInnsendingsvalg(Status innsendingsvalg) {
        this.innsendingsvalg = innsendingsvalg;
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

    public Integer getAntallSider() {
        return antallSider;
    }

    public String getskjemaNummer() {
        return skjemaNummer;
    }

    public void setskjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
    }

    public String getFillagerReferanse() {
        return fillagerReferanse;
    }

    public void setFillagerReferanse(String fillagerReferanse) {
        this.fillagerReferanse = fillagerReferanse;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public Map<String, String> getUrls() {
        return urls;
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
                .append(this.vedleggId, rhs.vedleggId)
                .append(this.soknadId, rhs.soknadId)
                .append(this.navn, rhs.navn)
                .append(this.storrelse, rhs.storrelse)
                .append(this.faktumId, rhs.faktumId)
                .append(this.skjemaNummer, rhs.skjemaNummer)
                .append(this.antallSider, rhs.antallSider)
                .append(this.data, rhs.data)
                .append(this.fillagerReferanse, rhs.fillagerReferanse)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(vedleggId)
                .append(soknadId)
                .append(navn)
                .append(storrelse)
                .append(faktumId)
                .append(skjemaNummer)
                .append(antallSider)
                .append(data)
                .append(fillagerReferanse)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("vedleggId", vedleggId)
                .append("soknadId", soknadId)
                .append("navn", navn)
                .append("storrelse", storrelse)
                .append("faktumId", faktumId)
                .append("skjemaNummer", skjemaNummer)
                .append("antallSider", antallSider)
                .append("data", data)
                .append("fillagerReferanse", fillagerReferanse)
                .toString();
    }

    public void leggTilInnhold(byte[] doc, int antallSider) {
        this.data = doc;
        this.innsendingsvalg = Status.LastetOpp;
        this.antallSider = antallSider;
        this.storrelse = (long) doc.length;
    }

    public void leggTilURL(String nokkel, String url) {
        urls.put(nokkel, url);
    }

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public enum Status {
        IkkeVedlegg,
        VedleggKreves,
        LastetOpp,
        UnderBehandling,
        SendesSenere,
        SendesIkke;

        public boolean er(Status status) {
            return this.equals(status);
        }
    }
}
