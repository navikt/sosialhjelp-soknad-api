package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ArbeidsavtaleDto.Builder.class)
public class ArbeidsavtaleDto {

    private final double stillingsprosent;

    public ArbeidsavtaleDto(double stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }

    public ArbeidsavtaleDto.Builder builder() {
        return new ArbeidsavtaleDto.Builder();
    }

    public double getStillingsprosent() {
        return stillingsprosent;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder {
        private double stillingsprosent;

        public Builder withStillingsprosent(double stillingsprosent) {
            this.stillingsprosent = stillingsprosent;
            return this;
        }

        public ArbeidsavtaleDto build() {
            return new ArbeidsavtaleDto(stillingsprosent);
        }
    }
}
