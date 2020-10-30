package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class PdlBarnResponse {

    private final PdlBarnData data;
    private final List<JsonNode> errors;

    @JsonCreator
    public PdlBarnResponse(
            @JsonProperty("data") PdlBarnData data,
            @JsonProperty("errors") List<JsonNode> errors
    ) {
        this.data = data;
        this.errors = errors;
    }

    public PdlBarnData getData() {
        return data;
    }

    public List<JsonNode> getErrors() {
        return errors;
    }
}
