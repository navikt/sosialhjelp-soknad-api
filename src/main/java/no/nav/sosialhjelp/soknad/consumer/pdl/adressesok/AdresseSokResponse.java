//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.databind.JsonNode;
//import no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlBaseResponse;
//
//import java.util.List;
//
//public class AdresseSokResponse extends PdlBaseResponse {
//
//    private final AdresseSok data;
//
//    @JsonCreator
//    public AdresseSokResponse(
//            @JsonProperty("data") AdresseSok data,
//            @JsonProperty("errors") List<JsonNode> errors
//    ) {
//        super(errors);
//        this.data = data;
//    }
//
//    public AdresseSok getData() {
//        return data;
//    }
//}
