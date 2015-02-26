package no.nav.sbl.dialogarena.soknadinnsending.business.selftest;

public class AvhengighetStatus {

    private final String navn;
    private final String status;
    private final long durationMilis;
    private final String beskrivelse;

    public AvhengighetStatus(String name, String status, long durationMilis, String beskrivelse) {
        this.navn = name;
        this.status = status;
        this.durationMilis = durationMilis;
        this.beskrivelse = beskrivelse;
    }

    public AvhengighetStatus(String name, String status, long durationMilis) {
        this(name, status, durationMilis, "");
    }

    public String getName() {
        return this.navn;
    }

    public String getStatus() {
        return this.status;
    }

    public long getDurationMilis() {
        return this.durationMilis;
    }

    public String getBeskrivelse() {
        return this.beskrivelse;
    }

}
