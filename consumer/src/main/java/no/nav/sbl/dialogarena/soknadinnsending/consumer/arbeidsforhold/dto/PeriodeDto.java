package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PeriodeDto {

    private LocalDate fom;
    private LocalDate tom;

    @JsonCreator
    public PeriodeDto(
            @JsonProperty("fom") LocalDate fom,
            @JsonProperty("tom") LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

//    public void setFom(LocalDate fom) {
//        this.fom = fom;
//    }
//
//    public void setTom(LocalDate tom) {
//        this.tom = tom;
//    }
}
