package no.nav.sbl.dialogarena.service.oppsummering;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

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
    private static final Logger logger = getLogger(OppsummeringsTyper.class);

    OppsummeringsTyper(String template) {
        this.template = template;
    }

    public static String resolve(String type) {
        try {
            return valueOf(type).template;
        } catch(IllegalArgumentException | NullPointerException e) {
            logger.info("Ukjent oppsumeringsstype, bruker default", e);
        }
        return radio.template;
    }
}
