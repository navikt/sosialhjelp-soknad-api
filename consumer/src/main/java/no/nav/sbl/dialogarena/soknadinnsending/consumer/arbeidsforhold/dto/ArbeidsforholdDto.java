package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArbeidsforholdDto {

    private AnsettelsesperiodeDto ansettelsesperiode;
    private List<ArbeidsavtaleDto> arbeidsavtaler;
    private String arbeidsforholdId;
    private OpplysningspliktigArbeidsgiverDto arbeidsgiver;
    private PersonDto arbeidstaker;
    private Long navArbeidsforholdId;
    private OpplysningspliktigArbeidsgiverDto opplysningspliktig;

    public ArbeidsforholdDto(AnsettelsesperiodeDto ansettelsesperiode, List<ArbeidsavtaleDto> arbeidsavtaler, String arbeidsforholdId, OpplysningspliktigArbeidsgiverDto arbeidsgiver, PersonDto arbeidstaker, Long navArbeidsforholdId, OpplysningspliktigArbeidsgiverDto opplysningspliktig) {
        this.ansettelsesperiode = ansettelsesperiode;
        this.arbeidsavtaler = arbeidsavtaler;
        this.arbeidsforholdId = arbeidsforholdId;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidstaker = arbeidstaker;
        this.navArbeidsforholdId = navArbeidsforholdId;
        this.opplysningspliktig = opplysningspliktig;
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

    public OpplysningspliktigArbeidsgiverDto getOpplysningspliktig() {
        return opplysningspliktig;
    }
}
