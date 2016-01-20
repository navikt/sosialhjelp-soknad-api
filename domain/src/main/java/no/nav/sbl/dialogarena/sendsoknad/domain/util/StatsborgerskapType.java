package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS_LAND;

public class StatsborgerskapType {

    public static String get(String landkode) {
        if ("NOR".equals(landkode)) {
            return "norsk";
        } else if (EOS_LAND.contains(landkode)) {
            return "eos";
        } else {
            return "ikkeEos";
        }
    }
}
