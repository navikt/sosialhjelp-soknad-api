//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;
//
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//public class VegadresseDto {
//
//    private final String matrikkelId;
//    private final Integer husnummer;
//    private final String husbokstav;
//    private final String adressenavn;
//    private final String kommunenavn;
//    private final String kommunenummer;
//    private final String postnummer;
//    private final String poststed;
//    private final String bydelsnummer;
//
//    @JsonCreator
//    public VegadresseDto(
//            @JsonProperty("matrikkelId") String matrikkelId,
//            @JsonProperty("husnummer") Integer husnummer,
//            @JsonProperty("husbokstav") String husbokstav,
//            @JsonProperty("adressenavn") String adressenavn,
//            @JsonProperty("kommunenavn") String kommunenavn,
//            @JsonProperty("kommunenummer") String kommunenummer,
//            @JsonProperty("postnummer") String postnummer,
//            @JsonProperty("poststed") String poststed,
//            @JsonProperty("bydelsnummer") String bydelsnummer
//    ) {
//        this.matrikkelId = matrikkelId;
//        this.husnummer = husnummer;
//        this.husbokstav = husbokstav;
//        this.adressenavn = adressenavn;
//        this.kommunenavn = kommunenavn;
//        this.kommunenummer = kommunenummer;
//        this.postnummer = postnummer;
//        this.poststed = poststed;
//        this.bydelsnummer = bydelsnummer;
//    }
//
//    public String getMatrikkelId() {
//        return matrikkelId;
//    }
//
//    public Integer getHusnummer() {
//        return husnummer;
//    }
//
//    public String getHusbokstav() {
//        return husbokstav;
//    }
//
//    public String getAdressenavn() {
//        return adressenavn;
//    }
//
//    public String getKommunenavn() {
//        return kommunenavn;
//    }
//
//    public String getKommunenummer() {
//        return kommunenummer;
//    }
//
//    public String getPostnummer() {
//        return postnummer;
//    }
//
//    public String getPoststed() {
//        return poststed;
//    }
//
//    public String getBydelsnummer() {
//        return bydelsnummer;
//    }
//}
