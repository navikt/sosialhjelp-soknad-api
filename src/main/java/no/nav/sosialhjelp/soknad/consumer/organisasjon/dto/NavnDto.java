package no.nav.sosialhjelp.soknad.consumer.organisasjon.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class NavnDto {

    private final String navnelinje1;
    private final String navnelinje2;
    private final String navnelinje3;
    private final String navnelinje4;
    private final String navnelinje5;
//    private String redigertnavn; // kunne kanskje brukt denne?

    @JsonCreator
    public NavnDto(
            @JsonProperty("navnelinje1") String navnelinje1,
            @JsonProperty("navnelinje2") String navnelinje2,
            @JsonProperty("navnelinje3") String navnelinje3,
            @JsonProperty("navnelinje4") String navnelinje4,
            @JsonProperty("navnelinje5") String navnelinje5) {
        this.navnelinje1 = navnelinje1;
        this.navnelinje2 = navnelinje2;
        this.navnelinje3 = navnelinje3;
        this.navnelinje4 = navnelinje4;
        this.navnelinje5 = navnelinje5;
    }

    public String getNavnelinje1() {
        return navnelinje1;
    }

    public String getNavnelinje2() {
        return navnelinje2;
    }

    public String getNavnelinje3() {
        return navnelinje3;
    }

    public String getNavnelinje4() {
        return navnelinje4;
    }

    public String getNavnelinje5() {
        return navnelinje5;
    }
}