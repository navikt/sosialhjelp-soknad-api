package no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KommuneInfo {

    @JsonProperty("kommunenummer")
    private String kommunenummer;
    @JsonProperty("kanMottaSoknader")
    private boolean kanMottaSoknader;
    @JsonProperty("kanOppdatereStatus")
    private boolean kanOppdatereStatus;
    @JsonProperty("harMidlertidigDeaktivertMottak")
    private boolean harMidlertidigDeaktivertMottak;
    @JsonProperty("harMidlertidigDeaktivertOppdateringer")
    private boolean harMidlertidigDeaktivertOppdateringer;

    @JsonProperty("kommunenummer")
    public String getKommunenummer() {
        return kommunenummer;
    }

    @JsonProperty("kommunenummer")
    public void setKommunenummer(String kommunenummer) {
        this.kommunenummer = kommunenummer;
    }

    @JsonProperty("kanMottaSoknader")
    public boolean getKanMottaSoknader() {
        return kanMottaSoknader;
    }

    @JsonProperty("kanMottaSoknader")
    public void setKanMottaSoknader(boolean kanMottaSoknader) {
        this.kanMottaSoknader = kanMottaSoknader;
    }

    @JsonProperty("kanOppdatereStatus")
    public boolean getKanOppdatereStatus() {
        return kanOppdatereStatus;
    }

    @JsonProperty("kanOppdatereStatus")
    public void setKanOppdatereStatus(boolean kanOppdatereStatus) {
        this.kanOppdatereStatus = kanOppdatereStatus;
    }

    @JsonProperty("harMidlertidigDeaktivertMottak")
    public boolean getHarMidlertidigDeaktivertMottak() {
        return harMidlertidigDeaktivertMottak;
    }

    @JsonProperty("harMidlertidigDeaktivertMottak")
    public void setHarMidlertidigDeaktivertMottak(boolean harMidlertidigDeaktivertMottak) {
        this.harMidlertidigDeaktivertMottak = harMidlertidigDeaktivertMottak;
    }

    @JsonProperty("harMidlertidigDeaktivertOppdateringer")
    public boolean getHarMidlertidigDeaktivertOppdateringer() {
        return harMidlertidigDeaktivertOppdateringer;
    }

    @JsonProperty("harMidlertidigDeaktivertOppdateringer")
    public void setHarMidlertidigDeaktivertOppdateringer(boolean harMidlertidigDeaktivertOppdateringer) {
        this.harMidlertidigDeaktivertOppdateringer = harMidlertidigDeaktivertOppdateringer;
    }

}