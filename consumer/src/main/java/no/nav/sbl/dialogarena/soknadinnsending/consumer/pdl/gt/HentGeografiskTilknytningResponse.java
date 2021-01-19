package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.gt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.common.PdlBaseResponse;

import java.util.List;

public class HentGeografiskTilknytningResponse extends PdlBaseResponse {

    private final HentGeografiskTilknytning data;

    @JsonCreator
    public HentGeografiskTilknytningResponse(
            @JsonProperty("data") HentGeografiskTilknytning data,
            @JsonProperty("errors") List<JsonNode> errors
    ) {
        super(errors);
        this.data = data;
    }

    public HentGeografiskTilknytning getData() {
        return data;
    }

}
