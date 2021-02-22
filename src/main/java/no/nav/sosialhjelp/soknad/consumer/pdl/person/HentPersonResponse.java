package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlBaseResponse;

import java.util.List;

public class HentPersonResponse<T> extends PdlBaseResponse {

    private final HentPerson<T> data;

    @JsonCreator
    public HentPersonResponse(
            @JsonProperty("data") HentPerson<T> data,
            @JsonProperty("errors") List<JsonNode> errors
    ) {
        super(errors);
        this.data = data;
    }

    public HentPerson<T> getData() {
        return data;
    }

}
