package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = PeriodeDto.Builder.class)
public class PeriodeDto {

    private final LocalDate fom;
    private final LocalDate tom;

    public PeriodeDto(LocalDate fom, LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public Builder builder() {
        return new Builder();
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder {
        private LocalDate fom;
        private LocalDate tom;

        public Builder withFom(LocalDate fom) {
            this.fom = fom;
            return this;
        }

        public Builder withTom(LocalDate tom) {
            this.tom = tom;
            return this;
        }

        public PeriodeDto build() {
            return new PeriodeDto(fom, tom);
        }
    }
}
