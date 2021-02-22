package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KontaktadresseDto {

    private final String type;
    private final String coAdressenavn;
    private final VegadresseDto vegadresse;
    private final MetadataDto metadata;
    private final FolkeregistermetadataDto folkeregistermetadata;

    @JsonCreator
    public KontaktadresseDto(
            @JsonProperty("type") String type,
            @JsonProperty("coAdressenavn") String coAdressenavn,
            @JsonProperty("vegadresse") VegadresseDto vegadresse,
            @JsonProperty("metadata") MetadataDto metadata,
            @JsonProperty("folkeregistermetadata") FolkeregistermetadataDto folkeregistermetadata
    ) {
        this.type = type;
        this.coAdressenavn = coAdressenavn;
        this.vegadresse = vegadresse;
        this.metadata = metadata;
        this.folkeregistermetadata = folkeregistermetadata;
    }

    public String getType() {
        return type;
    }

    public String getCoAdressenavn() {
        return coAdressenavn;
    }

    public VegadresseDto getVegadresse() {
        return vegadresse;
    }

    public MetadataDto getMetadata() {
        return metadata;
    }

    public FolkeregistermetadataDto getFolkeregistermetadata() {
        return folkeregistermetadata;
    }

}
