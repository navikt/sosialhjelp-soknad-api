//package no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.dto;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//public class GeografiskTilknytningDto {
//
//    private final GtType gtType;
//    private final String gtKommune;
//    private final String gtBydel;
//    private final String gtLand;
//
//    @JsonCreator
//    public GeografiskTilknytningDto(
//            @JsonProperty("gtType") GtType gtType,
//            @JsonProperty("gtKommune") String gtKommune,
//            @JsonProperty("gtBydel") String gtBydel,
//            @JsonProperty("gtLand") String gtLand
//    ) {
//        this.gtType = gtType;
//        this.gtKommune = gtKommune;
//        this.gtBydel = gtBydel;
//        this.gtLand = gtLand;
//    }
//
//    public GtType getGtType() {
//        return gtType;
//    }
//
//    public String getGtKommune() {
//        return gtKommune;
//    }
//
//    public String getGtBydel() {
//        return gtBydel;
//    }
//
//    public String getGtLand() {
//        return gtLand;
//    }
//}
