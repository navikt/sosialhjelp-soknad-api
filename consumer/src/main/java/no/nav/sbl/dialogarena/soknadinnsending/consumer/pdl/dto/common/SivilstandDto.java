package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SivilstandDto {

    private final SivilstandType type;
    private final String relatertVedSivilstand;
    private final MetadataDto metadata;
    private final FolkeregisterMetadataDto folkeregisterMetadata;

    @JsonCreator
    public SivilstandDto(
            @JsonProperty("type") SivilstandType type,
            @JsonProperty("relatertVedSivilstand") String relatertVedSivilstand,
            @JsonProperty("metadata") MetadataDto metadata,
            @JsonProperty("folkeregisterMetadata") FolkeregisterMetadataDto folkeregisterMetadata
    ) {
        this.type = type;
        this.relatertVedSivilstand = relatertVedSivilstand;
        this.metadata = metadata;
        this.folkeregisterMetadata = folkeregisterMetadata;
    }

    public SivilstandType getType() {
        return type;
    }

    public String getRelatertVedSivilstand() {
        return relatertVedSivilstand;
    }

    public MetadataDto getMetadata() {
        return metadata;
    }

    public FolkeregisterMetadataDto getFolkeregisterMetadata() {
        return folkeregisterMetadata;
    }

    public enum SivilstandType {
        UOPPGITT, UGIFT, GIFT, ENKE_ELLER_ENKEMANN, SKILT, SEPARERT, PARTNER, SEPARERT_PARTNER, SKILT_PARTNER, GJENLEVENDE_PARTNER
    }
}
