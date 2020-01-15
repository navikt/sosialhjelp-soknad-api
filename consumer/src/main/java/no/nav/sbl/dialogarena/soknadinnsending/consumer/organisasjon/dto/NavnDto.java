package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NavnDto {

    private String navnelinje1;
    private String navnelinje2;
    private String navnelinje3;
    private String navnelinje4;
    private String navnelinje5;
//    private String redigertnavn; // kunne kanskje brukt denne?


    public NavnDto() {
    }

    public NavnDto(String navnelinje1, String navnelinje2, String navnelinje3, String navnelinje4, String navnelinje5) {
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

    public void setNavnelinje1(String navnelinje1) {
        this.navnelinje1 = navnelinje1;
    }

    public void setNavnelinje2(String navnelinje2) {
        this.navnelinje2 = navnelinje2;
    }

    public void setNavnelinje3(String navnelinje3) {
        this.navnelinje3 = navnelinje3;
    }

    public void setNavnelinje4(String navnelinje4) {
        this.navnelinje4 = navnelinje4;
    }

    public void setNavnelinje5(String navnelinje5) {
        this.navnelinje5 = navnelinje5;
    }
}