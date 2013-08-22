package no.nav.sbl.dialogarena.websoknad.behaviors.util;

public final class UtilBehaviors {

    private UtilBehaviors() {}

    public static InputType hasType(String type) {
        return new InputType(type);
    }

    public static LabelFor labelFor(String type) {
        return new LabelFor(type);
    }
}