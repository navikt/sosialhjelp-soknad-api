package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FamilierelasjonDto {

    private final String relatertPersonsIdent;
    private final String relatertPersonsRolle;
    private final String minRolleForPerson;

    @JsonCreator
    public FamilierelasjonDto(
            @JsonProperty("relatertPersonsIdent") String relatertPersonsIdent,
            @JsonProperty("relatertPersonsRolle") String relatertPersonsRolle,
            @JsonProperty("minRolleForPerson") String minRolleForPerson
    ) {
        this.relatertPersonsIdent = relatertPersonsIdent;
        this.relatertPersonsRolle = relatertPersonsRolle;
        this.minRolleForPerson = minRolleForPerson;
    }

    public String getRelatertPersonsIdent() {
        return relatertPersonsIdent;
    }

    public String getRelatertPersonsRolle() {
        return relatertPersonsRolle;
    }

    public String getMinRolleForPerson() {
        return minRolleForPerson;
    }
}
