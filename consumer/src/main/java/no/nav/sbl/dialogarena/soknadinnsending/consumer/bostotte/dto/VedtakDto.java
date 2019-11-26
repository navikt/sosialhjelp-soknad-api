package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VedtakDto {
    @SuppressWarnings("WeakerAccess")
    public String kode;
    public String beskrivelse;
    public String type;

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public String getType() {
        return type;
    }

    public VedtakDto with(String kode, String beskrivelse, String type) {
        this.kode = kode;
        this.beskrivelse = beskrivelse;
        this.type = type;
        return this;
    }
}
