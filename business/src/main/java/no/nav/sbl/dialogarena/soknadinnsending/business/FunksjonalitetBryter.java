package no.nav.sbl.dialogarena.soknadinnsending.business;

public enum FunksjonalitetBryter {
    GammelVedleggsLogikk ("vedlegg.gammel.generering", "Gammel vedleggsgenerering");

    public final String nokkel;
    public final String beskrivelse;

    FunksjonalitetBryter(String nokkel, String beskrivelse) {
        this.nokkel = nokkel;
        this.beskrivelse = beskrivelse;
    }

    public boolean erAktiv(){
        return "true".equals(System.getProperty(nokkel, "false"));
    }
}
