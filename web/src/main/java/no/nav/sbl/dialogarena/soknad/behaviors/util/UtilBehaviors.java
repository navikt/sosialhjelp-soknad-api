package no.nav.sbl.dialogarena.soknad.behaviors.util;

public class UtilBehaviors {
    public static InputType hasType(String type) {
        return new InputType(type);
    }

    public static LabelFor labelFor(String type) {
        return new LabelFor(type);
    }
}
