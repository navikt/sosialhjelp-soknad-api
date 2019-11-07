package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

public class VedtakDto {
    public String kode;
    public String beskrivelse;
    public String status;

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public String getStatus() {
        return status;
    }

    public VedtakDto with(String kode, String beskrivelse, String status) {
        this.kode = kode;
        this.beskrivelse = beskrivelse;
        this.status = status;
        return this;
    }
}
