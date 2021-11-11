//package no.nav.sosialhjelp.soknad.consumer.kodeverk.dto;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.util.List;
//import java.util.Map;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class KodeverkDto {
//
//    private final Map<String, List<BetydningDto>> betydninger;
//
//    @JsonCreator
//    public KodeverkDto(
//            @JsonProperty("betydninger") Map<String, List<BetydningDto>> betydninger
//    ) {
//        this.betydninger = betydninger;
//    }
//
//    public Map<String, List<BetydningDto>> getBetydninger() {
//        return betydninger;
//    }
//}
