package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class PdlResponse {

    private final PdlData data;
    private final List<JsonNode> errors;

    @JsonCreator
    public PdlResponse(
            @JsonProperty("data") PdlData data,
            @JsonProperty("errors") List<JsonNode> errors
    ) {
        this.data = data;
        this.errors = errors;
    }

    public PdlData getData() {
        return data;
    }

    public List<JsonNode> getErrors() {
        return errors;
    }
}
