package no.nav.sbl.dialogarena.common.kodeverk;

import java.util.Map;

public interface Kodeverk {
    String ANNET = "N6";
    String KVITTERING = "L7";

    String getKode(String var1, Kodeverk.Nokkel var2);

    String getTittel(String var1);

    Map<Kodeverk.Nokkel, String> getKoder(String var1);

    boolean isEgendefinert(String var1);

    public static enum Nokkel {
        SKJEMANUMMER,
        GOSYS_ID,
        TEMA,
        TITTEL,
        TITTEL_EN,
        BESKRIVELSE,
        URL,
        URLENGLISH,
        URLNEWNORWEGIAN,
        URLPOLISH,
        URLFRENCH,
        URLSPANISH,
        URLGERMAN,
        URLSAMISK,
        VEDLEGGSID;

        private Nokkel() {
        }
    }
}