package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

public enum Kilde {
    SYSTEM("SYSTEM"),
    BRUKER("BRUKER");

    private final String text;

    private Kilde(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}