package no.nav.sbl.dialogarena.websoknad.pages;

public enum SkjemaBootstrapFile {
    GJENOPPTAK("Gjenopptak"),
    ETTERSENDING("Ettersending"),
    DAGPENGER("Dagpenger");

    private final String text;

    private SkjemaBootstrapFile(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
