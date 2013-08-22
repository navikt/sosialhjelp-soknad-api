package no.nav.sbl.dialogarena.dokumentinnsending.domain;

public class Skjema extends Dokument {

    private String skjemaId;
    private String gosysId;
    private String skjemanummer;

    public Skjema(Type type, String skjemaId) {
        super(type);
        this.skjemaId = skjemaId;
    }

    public String getSkjemaId() {
        return skjemaId;
    }

    public String getGosysId() {
        return gosysId;
    }

    public void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }

    public void setSkjemanummer(String skjemanummer) {
        this.skjemanummer = skjemanummer;
    }

    public String getSkjemanummer() {
        return skjemanummer;
    }
}