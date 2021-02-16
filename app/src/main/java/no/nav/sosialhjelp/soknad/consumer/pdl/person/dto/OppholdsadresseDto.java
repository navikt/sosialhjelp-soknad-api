package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OppholdsadresseDto {

    private final String oppholdAnnetSted;
    private final String coAdressenavn;
    private final VegadresseDto vegadresse;
    private final MetadataDto metadata;
    private final FolkeregistermetadataDto folkeregistermetadata;

    @JsonCreator
    public OppholdsadresseDto(
            @JsonProperty("oppholdAnnetSted") String oppholdAnnetSted,
            @JsonProperty("coAdressenavn") String coAdressenavn,
            @JsonProperty("vegadresse") VegadresseDto vegadresse,
            @JsonProperty("metadata") MetadataDto metadata,
            @JsonProperty("folkeregistermetadata") FolkeregistermetadataDto folkeregistermetadata
    ) {
        this.oppholdAnnetSted = oppholdAnnetSted;
        this.coAdressenavn = coAdressenavn;
        this.vegadresse = vegadresse;
        this.metadata = metadata;
        this.folkeregistermetadata = folkeregistermetadata;
    }

    public String getOppholdAnnetSted() {
        return oppholdAnnetSted;
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
