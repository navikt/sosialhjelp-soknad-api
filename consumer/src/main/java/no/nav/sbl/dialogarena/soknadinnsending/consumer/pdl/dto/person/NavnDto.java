package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

public class NavnDto {

    private final String fornavn;
    private final String mellomnavn;
    private final String etternavn;
    private final String forkortetNavn;

    public NavnDto(String fornavn, String mellomnavn, String etternavn, String forkortetNavn) {
        this.fornavn = fornavn;
        this.mellomnavn = mellomnavn;
        this.etternavn = etternavn;
        this.forkortetNavn = forkortetNavn;
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

    public String getForkortetNavn() {
        return forkortetNavn;
    }
}
