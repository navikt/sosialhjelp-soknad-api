package no.nav.sbl.dialogarena.service.oppsummering;


public enum OppsummeringsTyper {
    checkboxGroup("skjema/generisk/checkboxgroup"),
    textbox("skjema/generisk/textbox"),
    composite("skjema/generisk/composite"),
    periode("skjema/generisk/periode"),
    date("skjema/generisk/date"),
    tilleggsopplysninger("skjema/generisk/tilleggsopplysninger"),
    hidden("skjema/generisk/hidden"),
    inputgroup("skjema/generisk/inputgroup"),
    infotekst("skjema/generisk/infotekst"),
    radio("skjema/generisk/default");

    public final String template;

    OppsummeringsTyper(String template) {
        this.template = template;
    }

    public static String resolve(String type) {
        try {
            return valueOf(type).template;
        } catch (Exception ignore) {
        }
        return radio.template;
    }
}
