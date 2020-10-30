package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class PdlPersonResponse {

    private final PdlPersonData data;
    private final List<JsonNode> errors;

    @JsonCreator
    public PdlPersonResponse(
            @JsonProperty("data") PdlPersonData data,
            @JsonProperty("errors") List<JsonNode> errors
    ) {
        this.data = data;
        this.errors = errors;
    }

    public PdlPersonData getData() {
        return data;
    }

    public List<JsonNode> getErrors() {
        return errors;
    }
}
