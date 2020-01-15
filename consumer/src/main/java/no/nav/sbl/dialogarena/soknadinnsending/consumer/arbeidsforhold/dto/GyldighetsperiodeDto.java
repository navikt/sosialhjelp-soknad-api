package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GyldighetsperiodeDto extends PeriodeDto {

    public GyldighetsperiodeDto(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }

}
