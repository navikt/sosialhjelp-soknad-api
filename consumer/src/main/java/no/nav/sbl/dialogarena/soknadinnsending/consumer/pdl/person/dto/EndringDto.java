package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndringDto {

    private final String kilde;
    private final LocalDateTime registrert;
    private final String registrertAv;
    private final String systemkilde;
    private final String type;

    @JsonCreator
    public EndringDto(
            @JsonProperty("kilde") String kilde,
            @JsonProperty("registrert") LocalDateTime registrert,
            @JsonProperty("registrertAv") String registrertAv,
            @JsonProperty("systemkilde") String systemkilde,
            @JsonProperty("type") String type) {
        this.kilde = kilde;
        this.registrert = registrert;
        this.registrertAv = registrertAv;
        this.systemkilde = systemkilde;
        this.type = type;
    }

    public String getKilde() {
        return kilde;
    }

    public LocalDateTime getRegistrert() {
        return registrert;
    }

    public String getRegistrertAv() {
        return registrertAv;
    }

    public String getSystemkilde() {
        return systemkilde;
    }

    public String getType() {
        return type;
    }
}
