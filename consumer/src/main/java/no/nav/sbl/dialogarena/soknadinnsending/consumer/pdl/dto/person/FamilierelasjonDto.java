package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;

public class FamilierelasjonDto {

    private final String relatertPersonsIdent;
    private final String relatertPersonsRolle;
    private final String minRolleForPerson;

    @JsonCreator
    public FamilierelasjonDto(String relatertPersonsIdent, String relatertPersonsRolle, String minRolleForPerson) {
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
