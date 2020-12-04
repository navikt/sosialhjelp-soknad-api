package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataDto {

    private final String master;
    private final String opplysningsId;
    private final List<EndringDto> endringer;

    @JsonCreator
    public MetadataDto(
            @JsonProperty("master") String master,
            @JsonProperty("opplysningsId") String opplysningsId,
            @JsonProperty("endringer") List<EndringDto> endringer) {
        this.master = master;
        this.opplysningsId = opplysningsId;
        this.endringer = endringer;
    }

    public String getMaster() {
        return master;
    }

    public String getOpplysningsId() {
        return opplysningsId;
    }

    public List<EndringDto> getEndringer() {
        return endringer;
    }
}
