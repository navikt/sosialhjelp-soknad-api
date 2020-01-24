package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrganisasjonDto.class, name = "Organisasjon"),
        @JsonSubTypes.Type(value = PersonDto.class, name = "Person")
})
public interface OpplysningspliktigArbeidsgiverDtoMixIn {
}