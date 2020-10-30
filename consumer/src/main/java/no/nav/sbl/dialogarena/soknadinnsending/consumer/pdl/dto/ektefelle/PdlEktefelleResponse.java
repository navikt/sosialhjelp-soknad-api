package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class PdlEktefelleResponse {

    private final PdlEktefelleData data;
    private final List<JsonNode> errors;

    @JsonCreator
    public PdlEktefelleResponse(
            @JsonProperty("data") PdlEktefelleData data,
            @JsonProperty("errors") List<JsonNode> errors
    ) {
        this.data = data;
        this.errors = errors;
    }

    public PdlEktefelleData getData() {
        return data;
    }

    public List<JsonNode> getErrors() {
        return errors;
    }
}
