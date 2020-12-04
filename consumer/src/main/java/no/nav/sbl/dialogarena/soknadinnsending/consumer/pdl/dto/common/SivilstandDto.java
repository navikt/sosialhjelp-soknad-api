package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SivilstandDto {

    private final SivilstandType type;
    private final String relatertVedSivilstand;
    private final MetadataDto metadata;
    private final FolkeregistermetadataDto folkeregistermetadata;

    @JsonCreator
    public SivilstandDto(
            @JsonProperty("type") SivilstandType type,
            @JsonProperty("relatertVedSivilstand") String relatertVedSivilstand,
            @JsonProperty("metadata") MetadataDto metadata,
            @JsonProperty("folkeregistermetadata") FolkeregistermetadataDto folkeregistermetadata
    ) {
        this.type = type;
        this.relatertVedSivilstand = relatertVedSivilstand;
        this.metadata = metadata;
        this.folkeregistermetadata = folkeregistermetadata;
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

    public FolkeregistermetadataDto getFolkeregistermetadata() {
        return folkeregistermetadata;
    }

    public enum SivilstandType {
        UOPPGITT, UGIFT, GIFT, ENKE_ELLER_ENKEMANN, SKILT, SEPARERT, PARTNER, SEPARERT_PARTNER, SKILT_PARTNER, GJENLEVENDE_PARTNER
    }
}
