package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArbeidsforholdDto {

    private AnsettelsesperiodeDto ansettelsesperiode;
    private List<ArbeidsavtaleDto> arbeidsavtaler;
    private String arbeidsforholdId;
    private OpplysningspliktigArbeidsgiverDto arbeidsgiver;
    private PersonDto arbeidstaker;
    private Long navArbeidsforholdId;

    @JsonCreator
    public ArbeidsforholdDto(
            @JsonProperty("ansettelsesperiode") AnsettelsesperiodeDto ansettelsesperiode,
            @JsonProperty("arbeidsavtaler") List<ArbeidsavtaleDto> arbeidsavtaler,
            @JsonProperty("arbeidsforholdId") String arbeidsforholdId,
            @JsonProperty("arbeidsgiver") OpplysningspliktigArbeidsgiverDto arbeidsgiver,
            @JsonProperty("arbeidstaker") PersonDto arbeidstaker,
            @JsonProperty("navArbeidsforholdId") Long navArbeidsforholdId) {
        this.ansettelsesperiode = ansettelsesperiode;
        this.arbeidsavtaler = arbeidsavtaler;
        this.arbeidsforholdId = arbeidsforholdId;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidstaker = arbeidstaker;
        this.navArbeidsforholdId = navArbeidsforholdId;
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

}
