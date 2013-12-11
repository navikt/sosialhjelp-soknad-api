package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Barn {
    private Long soknadId;
    private String fnr;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String kjonn;
    private String sammensattnavn;
    private Integer alder;

    public Barn(Long soknadId, String fnr, String fornavn, String mellomnavn, String etternavn) {
        this.soknadId = soknadId;
        this.fnr = fnr;
        this.fornavn = fornavn;
        this.mellomnavn = mellomnavn;
        this.etternavn = etternavn;
        this.sammensattnavn = setSammenSattNavn(fornavn, mellomnavn, etternavn);
        this.kjonn = bestemmKjonn();
        this.alder = bestemmAlder();
    }

    private Integer bestemmAlder() {
        PersonAlder personAlder = new PersonAlder(fnr);
        return personAlder.getAlder();
    }

    /**
     * Siffer 9 i fodselsnummeret indikerer om personen er gutt eller jente. Jenter har partall, gutter oddetall.
     *
     * @return
     */
    private String bestemmKjonn() {
        String kjonnSiffer = fnr.substring(8, 9);

        if (Integer.parseInt(kjonnSiffer) % 2 == 0) {
            return "jente";
        } else {
            return "gutt";
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
}
