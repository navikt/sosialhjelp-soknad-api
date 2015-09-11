package no.nav.sbl.dialogarena.soknadinnsending.business.domain;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Barn {
    private Long soknadId;
    private String doedsdato;
    private String personstatus;
    private String fnr;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String kjonn;
    private String sammensattnavn;
    private String land;
    private Integer alder;

    private static final String KJONN_MANN = "m";
    private static final String KJONN_KVINNE = "k";

    public Barn(Long soknadId, String doedsdato, String personstatus, String fnr, String fornavn, String mellomnavn, String etternavn) {
        this.soknadId = soknadId;
        this.doedsdato = doedsdato;
        this.personstatus = personstatus;
        this.fnr = fnr;
        this.fornavn = fornavn;
        this.mellomnavn = mellomnavn;
        this.etternavn = etternavn;
        this.sammensattnavn = setSammenSattNavn(fornavn, mellomnavn, etternavn);
        this.kjonn = bestemKjonn();
        this.alder = bestemAlder();
    }

    private Integer bestemAlder() {
        PersonAlder personAlder = new PersonAlder(fnr);
        return personAlder.getAlder();
    }

    /**
     * Siffer 9 i fodselsnummeret indikerer om personen er gutt eller jente. Jenter har partall, gutter oddetall.
     *
     * @return
     */
    private String bestemKjonn() {
        String kjonnSiffer = fnr.substring(8, 9);

        if (Integer.parseInt(kjonnSiffer) % 2 == 0) {
            return KJONN_KVINNE;
        } else {
            return KJONN_MANN;
        }
    }

    private String setSammenSattNavn(String fornavn, String mellomnavn, String etternavn) {
        if ("".equals(fornavn) || fornavn == null) {
            return etternavn;
        } else if ("".equals(mellomnavn) || mellomnavn == null) {
            return fornavn + " " + etternavn;
        } else {
            return fornavn + " " + mellomnavn + " " + etternavn;
        }
    }

    public String getFnr() {
        return fnr;
    }

    public String getFornavn() {
        return fornavn;
    }

    public String getMellomnavn() {
        return mellomnavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public String getKjonn() {
        return kjonn;
    }

    public Integer getAlder() {
        return alder;
    }

    public String getSammensattnavn() {
        return sammensattnavn;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public Barn withLand(String land) {
        this.land = land;
        return this;
    }

    public String getDoedsdato() {
        return doedsdato;
    }

    public void setDoedsdato(String doedsdato) {
        this.doedsdato = doedsdato;
    }

    public String getPersonstatus() {
        return personstatus;
    }

    public void setPersonstatus(String personstatus) {
        this.personstatus = personstatus;
    }
}
