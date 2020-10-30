package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NavnDto {

    private final String fornavn;
    private final String mellomnavn;
    private final String etternavn;

    @JsonCreator
    public NavnDto(
            @JsonProperty("fornavn") String fornavn,
            @JsonProperty("mellomnavn") String mellomnavn,
            @JsonProperty("etternavn") String etternavn
    ) {
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
