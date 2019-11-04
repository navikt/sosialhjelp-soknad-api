package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KommuneInfo {

    @JsonProperty("kommunenummer")
    private String kommunenummer;
    @JsonProperty("kanMottaSoknader")
    private Boolean kanMottaSoknader;
    @JsonProperty("kanOppdatereStatus")
    private Boolean kanOppdatereStatus;
    @JsonProperty("harMidlertidigDeaktivertMottak")
    private Boolean harMidlertidigDeaktivertMottak;
    @JsonProperty("harMidlertidigDeaktivertOppdateringer")
    private Boolean harMidlertidigDeaktivertOppdateringer;

    @JsonProperty("kommunenummer")
    public String getKommunenummer() {
        return kommunenummer;
    }

    @JsonProperty("kommunenummer")
    public void setKommunenummer(String kommunenummer) {
        this.kommunenummer = kommunenummer;
    }

    @JsonProperty("kanMottaSoknader")
    public Boolean getKanMottaSoknader() {
        return kanMottaSoknader;
    }

    @JsonProperty("kanMottaSoknader")
    public void setKanMottaSoknader(Boolean kanMottaSoknader) {
        this.kanMottaSoknader = kanMottaSoknader;
    }

    @JsonProperty("kanOppdatereStatus")
    public Boolean getKanOppdatereStatus() {
        return kanOppdatereStatus;
    }

    @JsonProperty("kanOppdatereStatus")
    public void setKanOppdatereStatus(Boolean kanOppdatereStatus) {
        this.kanOppdatereStatus = kanOppdatereStatus;
    }

    @JsonProperty("harMidlertidigDeaktivertMottak")
    public Boolean getHarMidlertidigDeaktivertMottak() {
        return harMidlertidigDeaktivertMottak;
    }

    @JsonProperty("harMidlertidigDeaktivertMottak")
    public void setHarMidlertidigDeaktivertMottak(Boolean harMidlertidigDeaktivertMottak) {
        this.harMidlertidigDeaktivertMottak = harMidlertidigDeaktivertMottak;
    }

    @JsonProperty("harMidlertidigDeaktivertOppdateringer")
    public Boolean getHarMidlertidigDeaktivertOppdateringer() {
        return harMidlertidigDeaktivertOppdateringer;
    }

    @JsonProperty("harMidlertidigDeaktivertOppdateringer")
    public void setHarMidlertidigDeaktivertOppdateringer(Boolean harMidlertidigDeaktivertOppdateringer) {
        this.harMidlertidigDeaktivertOppdateringer = harMidlertidigDeaktivertOppdateringer;
    }

}