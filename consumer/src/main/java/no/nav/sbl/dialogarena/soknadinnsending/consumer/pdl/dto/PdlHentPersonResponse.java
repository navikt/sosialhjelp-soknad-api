package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class PdlHentPersonResponse<T> {

    private final PdlHentPerson<T> data;
    private final List<JsonNode> errors;

    @JsonCreator
    public PdlHentPersonResponse(
            @JsonProperty("data") PdlHentPerson<T> data,
            @JsonProperty("errors") List<JsonNode> errors
    ) {
        this.data = data;
        this.errors = errors;
    }

    public PdlHentPerson<T> getData() {
        return data;
    }

    public List<JsonNode> getErrors() {
        return errors;
    }

}
