package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataDto {

    private final String master;
    private final List<EndringDto> endringer;

    @JsonCreator
    public MetadataDto(
            @JsonProperty("master") String master,
            @JsonProperty("endringer") List<EndringDto> endringer) {
        this.master = master;
        this.endringer = endringer;
    }

    public String getMaster() {
        return master;
    }

    public List<EndringDto> getEndringer() {
        return endringer;
    }
}
