package no.nav.sbl.dialogarena.soknad.behaviors;

public class UtilBehaviors {
    public static ConditionalTextFieldType hasType(String type) {
        return new ConditionalTextFieldType(type);
    }
}
