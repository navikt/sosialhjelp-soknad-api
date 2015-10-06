package no.nav.sbl.dialogarena.soknadinnsending.business;

public enum FunksjonalitetBryter {
    GammelVedleggsLogikk ("vedlegg.gammel.generering");
    private final String nokkel;
    FunksjonalitetBryter(String nokkel) {
        this.nokkel = nokkel;
    }

    public boolean erAktiv(){
        return "true".equals(System.getProperty(nokkel, "false"));
    }
}
