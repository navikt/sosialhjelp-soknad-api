package no.nav.sbl.dialogarena.soknad.convert;

public interface InputElement {

    String getKey();

    String getValue();

    String getType();

    Boolean isVisible();

    Boolean isModifiable();
}
