package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalKontaktinfoBolk {

    private List<DigitalKontaktinfo> kontaktinfo;
    private List<Feil> feil;

    @JsonCreator
    public DigitalKontaktinfoBolk(
            @JsonProperty("kontaktinfo") List<DigitalKontaktinfo> kontaktinfo,
            @JsonProperty("feil") List<Feil> feil) {
        this.kontaktinfo = kontaktinfo;
        this.feil = feil;
    }

    public List<DigitalKontaktinfo> getKontaktinfo() {
        return kontaktinfo;
    }

    public List<Feil> getFeil() {
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
