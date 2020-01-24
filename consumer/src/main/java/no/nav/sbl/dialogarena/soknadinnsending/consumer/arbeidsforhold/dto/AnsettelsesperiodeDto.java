package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = AnsettelsesperiodeDto.Builder.class)
public class AnsettelsesperiodeDto {

    private final PeriodeDto periode;

    public AnsettelsesperiodeDto(PeriodeDto periode) {
        this.periode = periode;
    }

    public AnsettelsesperiodeDto.Builder builder() {
        return new AnsettelsesperiodeDto.Builder();
    }

    public PeriodeDto getPeriode() {
        return periode;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder {
        private PeriodeDto periode;

        public Builder withPeriode(PeriodeDto periode) {
            this.periode = periode;
            return this;
        }

        public AnsettelsesperiodeDto build() {
            return new AnsettelsesperiodeDto(periode);
        }
    }
}
