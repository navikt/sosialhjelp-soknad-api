package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NavnDto {

    private final String fornavn;
    private final String mellomnavn;
    private final String etternavn;
    private final MetadataDto metadata;
    private final FolkeregistermetadataDto folkeregistermetadata;

    @JsonCreator
    public NavnDto(
            @JsonProperty("fornavn") String fornavn,
            @JsonProperty("mellomnavn") String mellomnavn,
            @JsonProperty("etternavn") String etternavn,
            @JsonProperty("metadata") MetadataDto metadata,
            @JsonProperty("folkeregistermetadata") FolkeregistermetadataDto folkeregistermetadata
    ) {
        this.fornavn = fornavn;
        this.mellomnavn = mellomnavn;
        this.etternavn = etternavn;
        this.metadata = metadata;
        this.folkeregistermetadata = folkeregistermetadata;
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

    public MetadataDto getMetadata() {
        return metadata;
    }

    public FolkeregistermetadataDto getFolkeregistermetadata() {
        return folkeregistermetadata;
    }
}
