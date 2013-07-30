package no.nav.sbl.dialogarena.soknad.convert;

public interface InputElement {

    public abstract String getKey();

    public abstract String getValue();

    public abstract Boolean isVisible();

    public abstract Boolean isModifiable();
}
