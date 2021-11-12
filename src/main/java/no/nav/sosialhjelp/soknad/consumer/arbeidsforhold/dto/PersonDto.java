//package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class PersonDto extends OpplysningspliktigArbeidsgiverDto {
//
//    private final String offentligIdent;
//    private final String aktoerId;
//    private final String type;
//
//    @JsonCreator
//    public PersonDto(
//            @JsonProperty("offentligIdent") String offentligIdent,
//            @JsonProperty("aktoerId") String aktoerId,
//            @JsonProperty("type") String type) {
//        this.offentligIdent = offentligIdent;
//        this.aktoerId = aktoerId;
//        this.type = type;
//    }
//
//    public String getOffentligIdent() {
//        return offentligIdent;
//    }
//
//    public String getAktoerId() {
//        return aktoerId;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//}
