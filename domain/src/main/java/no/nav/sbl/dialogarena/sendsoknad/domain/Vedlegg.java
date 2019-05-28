package no.nav.sbl.dialogarena.sendsoknad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.IkkeVedlegg;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Vedlegg {
    public static final Predicate<Vedlegg> PAAKREVDE_VEDLEGG = vedlegg -> vedlegg != null && !vedlegg.getInnsendingsvalg().equals(IkkeVedlegg);
    private Long vedleggId;
    private Long soknadId;
    private Long faktumId;
    private String skjemaNummer;
    private String skjemanummerTillegg;
    private Status innsendingsvalg;
    private Status opprinneligInnsendingsvalg;
    private String navn = "";
    private Long storrelse = 0L;
    private Integer antallSider = 0;
    private Long opprettetDato;
    private byte[] data;
    private String fillagerReferanse = UUID.randomUUID().toString();
    private Map<String, String> urls = new HashMap<>();
    private String tittel;
    private String aarsak;
    private String filnavn;
    private String mimetype;
    private String sha512;

    public Vedlegg() {
        this.sha512 = "";
    }

    public Vedlegg(Long soknadId, Long faktumId, String skjemaNummer, Status innsendingsvalg) {
        this.soknadId = soknadId;
        this.faktumId = faktumId;
        this.skjemaNummer = skjemaNummer;
        this.innsendingsvalg = innsendingsvalg;
        this.sha512 = "";
    }

    public Vedlegg medVedleggId(Long vedleggId) {
        this.vedleggId = vedleggId;
        return this;
    }

    public Vedlegg medSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        return this;
    }

    public Vedlegg medFaktumId(Long faktumId) {
        this.faktumId = faktumId;
        return this;
    }

    public Vedlegg medSkjemaNummer(String skjemaNummer) {
        setSkjemaNummer(skjemaNummer);
        return this;
    }

    public Vedlegg medSkjemanummerTillegg(String tillegg) {
        setSkjemanummerTillegg(tillegg);
        return this;
    }

    public Vedlegg medNavn(String navn) {
        setNavn(navn);
        return this;
    }

    public Vedlegg medStorrelse(Long storrelse) {
        this.storrelse = storrelse;
        return this;
    }

    public Vedlegg medAntallSider(Integer antallSider) {
        this.antallSider = antallSider;
        return this;
    }

    public Vedlegg medFillagerReferanse(String fillagerReferanse) {
        setFillagerReferanse(fillagerReferanse);
        return this;
    }

    public Vedlegg medData(byte[] data) {
        setData(data);
        return this;
    }

    public Vedlegg medSha512(String sha512) {
        this.sha512 = sha512;
        return this;
    }

    public Vedlegg medOpprettetDato(Long opprettetDato) {
        setOpprettetDato(opprettetDato);
        return this;
    }

    public Vedlegg medAarsak(String aarsak) {
        this.setAarsak(aarsak);
        return this;
    }

    public Vedlegg medFilnavn(String filnavn) {
        this.setFilnavn(filnavn);
        return this;
    }

    public Vedlegg medMimetype(String mimetype) {
        this.setMimetype(mimetype);
        return this;
    }

    public Vedlegg medInnsendingsvalg(Status innsendingsvalg) {
        this.innsendingsvalg = innsendingsvalg;
        return this;
    }

    public Vedlegg medOpprinneligInnsendingsvalg(Status opprinneligInnsendingsvalg) {
        this.opprinneligInnsendingsvalg = opprinneligInnsendingsvalg;
        return this;
    }


    public String getSha512() {
        return sha512;
    }

    public String getAarsak() {
        return this.aarsak;
    }

    public void setAarsak(String aarsak) {
        this.aarsak = aarsak;
    }

    public Status getOpprinneligInnsendingsvalg() {
        return opprinneligInnsendingsvalg;
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

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public void setFaktumId(Long faktumId) {
        this.faktumId = faktumId;
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

    public String getSkjemaNummer() {
        return skjemaNummer;
    }

    public void setSkjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
    }

    public String getSkjemanummerTillegg() {
        return this.skjemanummerTillegg;
    }

    public void setSkjemanummerTillegg(String tillegg) {
        this.skjemanummerTillegg = tillegg;
    }

    public String getFillagerReferanse() {
        return fillagerReferanse;
    }

    public void setFillagerReferanse(String fillagerReferanse) {
        this.fillagerReferanse = fillagerReferanse;
    }

    public Map<String, String> getUrls() {
        return urls;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public void setFilnavn(String filnavn) {
        this.filnavn = filnavn;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    @XmlTransient
    @JsonIgnore
    public byte[] getData() {
        return data.clone();
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
                .append(this.faktumId, rhs.faktumId)
                .append(this.skjemaNummer, rhs.skjemaNummer)
                .append(this.skjemanummerTillegg, rhs.skjemanummerTillegg)
                .append(this.innsendingsvalg, rhs.innsendingsvalg)
                .append(this.opprinneligInnsendingsvalg, rhs.opprinneligInnsendingsvalg)
                .append(this.navn, rhs.navn)
                .append(this.storrelse, rhs.storrelse)
                .append(this.antallSider, rhs.antallSider)
                .append(this.opprettetDato, rhs.opprettetDato)
                .append(this.data, rhs.data)
                .append(this.fillagerReferanse, rhs.fillagerReferanse)
                .append(this.urls, rhs.urls)
                .append(this.tittel, rhs.tittel)
                .append(this.aarsak, rhs.aarsak)
                .append(this.filnavn, rhs.filnavn)
                .append(this.mimetype, rhs.mimetype)
                .append(this.sha512, rhs.sha512)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(vedleggId)
                .append(soknadId)
                .append(faktumId)
                .append(skjemaNummer)
                .append(skjemanummerTillegg)
                .append(innsendingsvalg)
                .append(opprinneligInnsendingsvalg)
                .append(navn)
                .append(storrelse)
                .append(antallSider)
                .append(opprettetDato)
                .append(data)
                .append(fillagerReferanse)
                .append(urls)
                .append(tittel)
                .append(aarsak)
                .append(filnavn)
                .append(mimetype)
                .append(sha512)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("vedleggId", vedleggId)
                .append("soknadId", soknadId)
                .append("faktumId", faktumId)
                .append("skjemaNummer", skjemaNummer)
                .append("skjemanummerTillegg", skjemanummerTillegg)
                .append("innsendingsvalg", innsendingsvalg)
                .append("opprinneligInnsendingsvalg", opprinneligInnsendingsvalg)
                .append("navn", navn)
                .append("storrelse", storrelse)
                .append("antallSider", antallSider)
                .append("opprettetDato", opprettetDato)
                .append("data", data)
                .append("fillagerReferanse", fillagerReferanse)
                .append("urls", urls)
                .append("tittel", tittel)
                .append("aarsak", aarsak)
                .append("filnavn", filnavn)
                .append("mimetype", mimetype)
                .append("sha512", sha512)
                .toString();
    }

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public Long getOpprettetDato() {
        return opprettetDato;
    }

    public void setOpprettetDato(Long opprettetDato) {
        this.opprettetDato = opprettetDato;
    }

    public void setData(byte[] data) {
        this.data = data != null ? data.clone() : null;
        this.sha512 = ServiceUtils.getSha512FromByteArray(data);
    }

    @JsonIgnore
    public String lagFilNavn() {
        if (isEmpty(filnavn)) {
            return getSkjemaNummer().equals("N6") ? getNavn() : getSkjemaNummer();
        } else {
            return filnavn;
        }
    }


    /**
     * SendesIkke er en legacy-status som ikke lengre skal være mulig å velge.
     */
    public enum Status {
        SendesIkke(-1),

        IkkeVedlegg(0),
        VedleggKreves(1),
        VedleggSendesIkke(2),
        VedleggSendesAvAndre(3),
        SendesSenere(4),
        LastetOpp(5),
        UnderBehandling(6),
        VedleggAlleredeSendt(7);

        private int prioritet;

        private Status(int prioritet) {
            this.prioritet = prioritet;
        }

        public int getPrioritet() {
            return prioritet;
        }

        public boolean er(Status status) {
            return this.equals(status);
        }

        public boolean erIkke(Status status) {
            return !this.er(status);
        }
    }
}
