package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

public class VedtakDto {
    private String kode;
    private String beskrivelse;

    public String getKode() {
        return kode;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public VedtakDto with(String kode, String beskrivelse) {
        this.kode = kode;
        this.beskrivelse = beskrivelse;
        return this;
    }
}
