package no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

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
    @JsonProperty("kontaktpersoner")
    private Kontaktpersoner kontaktpersoner;
    @JsonProperty("harNksTilgang")
    private boolean harNksTilgang;
    @JsonProperty("behandlingsansvarlig")
    private String behandlingsansvarlig; //Nullable

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

    @JsonProperty("kontaktpersoner")
    public Kontaktpersoner getKontaktpersoner() {
        return kontaktpersoner;
    }

    @JsonProperty("kontaktpersoner")
    public void setKontaktpersoner(Kontaktpersoner kontaktpersoner) {
        this.kontaktpersoner = kontaktpersoner;
    }

    @JsonProperty("harNksTilgang")
    public boolean isHarNksTilgang() {
        return harNksTilgang;
    }
    @JsonProperty("harNksTilgang")
    public void setHarNksTilgang(boolean harNksTilgang) {
        this.harNksTilgang = harNksTilgang;
    }

    @JsonProperty("behandlingsansvarlig")
    public String getBehandlingsansvarlig() {
        return behandlingsansvarlig;
    }

    @JsonProperty("behandlingsansvarlig")
    public void setBehandlingsansvarlig(String behandlingsansvarlig) {
        this.behandlingsansvarlig = behandlingsansvarlig;
    }

    @Override
    public String toString() {
        return "{" +
                "kommunenummer='" + kommunenummer + '\'' +
                ", kanMottaSoknader=" + kanMottaSoknader +
                ", kanOppdatereStatus=" + kanOppdatereStatus +
                ", harMidlertidigDeaktivertMottak=" + harMidlertidigDeaktivertMottak +
                ", harMidlertidigDeaktivertOppdateringer=" + harMidlertidigDeaktivertOppdateringer +
                ", kontaktpersoner=" + kontaktpersoner +
                ", harNksTilgang=" + harNksTilgang +
                ", behandlingsansvarlig='" + behandlingsansvarlig + '\'' +
                '}';
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Kontaktpersoner {
    @JsonProperty("fagansvarligEpost")
    private String[] fagansvarligEpost;
    @JsonProperty("tekniskAnsvarligEpost")
    private String[] tekniskAnsvarligEpost;

    public String[] getFagansvarligEpost() {
        return fagansvarligEpost;
    }

    public void setFagansvarligEpost(String[] fagansvarligEpost) {
        this.fagansvarligEpost = fagansvarligEpost;
    }

    public String[] getTekniskAnsvarligEpost() {
        return tekniskAnsvarligEpost;
    }

    public void setTekniskAnsvarligEpost(String[] tekniskAnsvarligEpost) {
        this.tekniskAnsvarligEpost = tekniskAnsvarligEpost;
    }

    @Override
    public String toString() {
        return "{" +
                "fagansvarligEpost=" + Arrays.toString(fagansvarligEpost) +
                ", tekniskAnsvarligEpost=" + Arrays.toString(tekniskAnsvarligEpost) +
                '}';
    }
}

