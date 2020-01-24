package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ArbeidsforholdDto.Builder.class)
public class ArbeidsforholdDto {

    private final AnsettelsesperiodeDto ansettelsesperiode;
    private final List<ArbeidsavtaleDto> arbeidsavtaler;
    private final String arbeidsforholdId;
    private final OpplysningspliktigArbeidsgiverDto arbeidsgiver;
    private final PersonDto arbeidstaker;
    private final Long navArbeidsforholdId;

    public ArbeidsforholdDto(AnsettelsesperiodeDto ansettelsesperiode, List<ArbeidsavtaleDto> arbeidsavtaler, String arbeidsforholdId, OpplysningspliktigArbeidsgiverDto arbeidsgiver, PersonDto arbeidstaker, Long navArbeidsforholdId) {
        this.ansettelsesperiode = ansettelsesperiode;
        this.arbeidsavtaler = arbeidsavtaler;
        this.arbeidsforholdId = arbeidsforholdId;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidstaker = arbeidstaker;
        this.navArbeidsforholdId = navArbeidsforholdId;
    }

    public ArbeidsforholdDto.Builder builder() {
        return new ArbeidsforholdDto.Builder();
    }

    public AnsettelsesperiodeDto getAnsettelsesperiode() {
        return ansettelsesperiode;
    }

    public List<ArbeidsavtaleDto> getArbeidsavtaler() {
        return arbeidsavtaler;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public OpplysningspliktigArbeidsgiverDto getArbeidsgiver() {
        return arbeidsgiver;
    }

    public PersonDto getArbeidstaker() {
        return arbeidstaker;
    }

    public Long getNavArbeidsforholdId() {
        return navArbeidsforholdId;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder {
        private AnsettelsesperiodeDto ansettelsesperiode;
        private List<ArbeidsavtaleDto> arbeidsavtaler;
        private String arbeidsforholdId;
        private OpplysningspliktigArbeidsgiverDto arbeidsgiver;
        private PersonDto arbeidstaker;
        private Long navArbeidsforholdId;

        public ArbeidsforholdDto.Builder withAnsettelsesperiode(AnsettelsesperiodeDto ansettelsesperiode) {
            this.ansettelsesperiode = ansettelsesperiode;
            return this;
        }

        public ArbeidsforholdDto.Builder withArbeidsavtaler(List<ArbeidsavtaleDto> arbeidsavtaler) {
            this.arbeidsavtaler = arbeidsavtaler;
            return this;
        }

        public ArbeidsforholdDto.Builder withArbeidsforholdId(String arbeidsforholdId) {
            this.arbeidsforholdId = arbeidsforholdId;
            return this;
        }

        public ArbeidsforholdDto.Builder withArbeidsgiver(OpplysningspliktigArbeidsgiverDto arbeidsgiver) {
            this.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public ArbeidsforholdDto.Builder withArbeidstaker(PersonDto arbeidstaker) {
            this.arbeidstaker = arbeidstaker;
            return this;
        }

        public ArbeidsforholdDto.Builder withNavArbeidsforholdId(Long navArbeidsforholdId) {
            this.navArbeidsforholdId = navArbeidsforholdId;
            return this;
        }

        public ArbeidsforholdDto build() {
            return new ArbeidsforholdDto(ansettelsesperiode, arbeidsavtaler, arbeidsforholdId, arbeidsgiver, arbeidstaker, navArbeidsforholdId);
        }
    }
}
