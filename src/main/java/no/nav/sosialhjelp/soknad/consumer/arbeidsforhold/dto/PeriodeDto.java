//package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.time.LocalDate;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class PeriodeDto {
//
//    private final LocalDate fom;
//    private final LocalDate tom;
//
//    @JsonCreator
//    public PeriodeDto(
//            @JsonProperty("fom") LocalDate fom,
//            @JsonProperty("tom") LocalDate tom) {
//        this.fom = fom;
//        this.tom = tom;
//    }
//
//    public LocalDate getFom() {
//        return fom;
//    }
//
//    public LocalDate getTom() {
//        return tom;
//    }
//}
