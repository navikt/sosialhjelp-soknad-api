package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = PersonDto.Builder.class)
public class PersonDto extends OpplysningspliktigArbeidsgiverDto {

    private final String offentligIdent;
    private final String aktoerId;

    public PersonDto(String offentligIdent, String aktoerId) {
        this.offentligIdent = offentligIdent;
        this.aktoerId = aktoerId;
    }

    public PersonDto.Builder builder() {
        return new PersonDto.Builder();
    }

    public String getOffentligIdent() {
        return offentligIdent;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    @Override
    public String getType() {
        return "Organisasjon";
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder {
        private String offentligIdent;
        private String aktoerId;

        public PersonDto.Builder withOffentligIdent(String offentligIdent) {
            this.offentligIdent = offentligIdent;
            return this;
        }

        public PersonDto.Builder withAktoerId(String aktoerId) {
            this.aktoerId = aktoerId;
            return this;
        }

        public PersonDto build() {
            return new PersonDto(offentligIdent, aktoerId);
        }
    }
}
