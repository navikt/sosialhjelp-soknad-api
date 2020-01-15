package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDto extends OpplysningspliktigArbeidsgiverDto {

    private String offentligIdent;

    private String aktoerId;

    public PersonDto(String offentligIdent, String aktoerId) {
        this.offentligIdent = offentligIdent;
        this.aktoerId = aktoerId;
    }

    public String getOffentligIdent() {
        return offentligIdent;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    @Override
    public String getType() {
        return "Person";
    }
}
