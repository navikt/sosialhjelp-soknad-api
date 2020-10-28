package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NavnDto {

    private final String fornavn;
    private final String mellomnavn;
    private final String etternavn;

    @JsonCreator
    public NavnDto(String fornavn, String mellomnavn, String etternavn) {
        this.fornavn = fornavn;
        this.mellomnavn = mellomnavn;
        this.etternavn = etternavn;
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
}
