package no.nav.sbl.dialogarena.service.oppsummering;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public enum OppsummeringsTyper {
    checkboxGroup("skjema/generisk/checkboxgroup"),
    checkbox("skjema/generisk/checkbox"),
    textbox("skjema/generisk/textbox"),
    composite("skjema/generisk/composite"),
    periode("skjema/generisk/periode"),
    date("skjema/generisk/date"),
    country("skjema/generisk/country"),
    tilleggsopplysninger("skjema/generisk/tilleggsopplysninger"),
    hidden("skjema/generisk/hidden"),
    inputgroup("skjema/generisk/inputgroup"),
    infotekst("skjema/generisk/infotekst"),
    radio("skjema/generisk/default"),
    tekster("skjema/generisk/tekster"),
    string("skjema/generisk/default"),
    bool("skjema/generisk/default"),
    inlineproperties("skjema/generisk/inlineproperties");


    public final String template;
    private static final Logger logger = getLogger(OppsummeringsTyper.class);

    OppsummeringsTyper(String template) {
        this.template = template;
    }

    public static String resolve(String type) {
        if(type != null) {
            try {
                return valueOf(type).template;
            } catch(IllegalArgumentException e) {
                logger.info("Ukjent oppsummeringstype {}, bruker default", type);
            }
        }
        return radio.template;
    }
}
