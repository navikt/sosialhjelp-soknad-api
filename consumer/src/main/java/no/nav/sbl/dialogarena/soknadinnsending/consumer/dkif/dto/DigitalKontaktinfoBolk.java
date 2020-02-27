package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalKontaktinfoBolk {

    private Map<String, DigitalKontaktinfo> kontaktinfo;
    private Map<String, Feil> feil;

    @JsonCreator
    public DigitalKontaktinfoBolk(
            @JsonProperty("kontaktinfo") Map<String, DigitalKontaktinfo> kontaktinfo,
            @JsonProperty("feil") Map<String,Feil> feil) {
        this.kontaktinfo = kontaktinfo;
        this.feil = feil;
    }

    public Map<String, DigitalKontaktinfo> getKontaktinfo() {
        return kontaktinfo;
    }

    public Map<String, Feil> getFeil() {
        return feil;
    }

    @Override
    public String toString() {
        return "DigitalKontaktinfoBolk{" +
                "kontaktinfo=" + kontaktinfo +
                ", feil=" + feil +
                '}';
    }
}
