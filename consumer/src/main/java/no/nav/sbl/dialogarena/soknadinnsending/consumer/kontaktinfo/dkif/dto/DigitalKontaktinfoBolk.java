package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalKontaktinfoBolk {

    private DigitalKontaktinfo kontaktinfo;
    private Feil feil;

    @JsonCreator
    public DigitalKontaktinfoBolk(
            @JsonProperty("kontaktinfo") DigitalKontaktinfo kontaktinfo,
            @JsonProperty("feil") Feil feil) {
        this.kontaktinfo = kontaktinfo;
        this.feil = feil;
    }

    public DigitalKontaktinfo getKontaktinfo() {
        return kontaktinfo;
    }

    public Feil getFeil() {
        return feil;
    }
}
