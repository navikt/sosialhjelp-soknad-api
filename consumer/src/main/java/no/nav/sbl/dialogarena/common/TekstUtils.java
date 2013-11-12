package no.nav.sbl.dialogarena.common;

import org.apache.commons.collections15.Transformer;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class TekstUtils {

    public static final String NON_WORD_CHARACTER_REGEXP = "\\W";

    public static String fjernSpesialtegn(String tekst) {
        if (!isBlank(tekst)) {
            return tekst.replaceAll(NON_WORD_CHARACTER_REGEXP, "");
        } else {
            return tekst;
        }
    }

    public static Transformer<String, String> utenSpesialtegn() {
        return new UtenSpesialtegn();
    }

    private static final class UtenSpesialtegn implements Transformer<String, String>, Serializable {
        @Override
        public String transform(String tekst) {
            return fjernSpesialtegn(tekst);
        }
    }

    private TekstUtils() { }
}
