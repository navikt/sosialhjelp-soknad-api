package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS_LAND;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.NORDEN_LAND;

public class StatsborgerskapType {

    public static final String TYPE_NORSK = "norsk";
    public static final String TYPE_EOS = "eos";
    public static final String TYPE_NORDEN = "norden";
    public static final String TYPE_IKKE_EOS = "ikkeEos";

    public static String get(String landkode) {
        if ("NOR".equals(landkode)) {
            return TYPE_NORSK;
        } else if (EOS_LAND.contains(landkode)) {
            return TYPE_EOS;
        } else if (NORDEN_LAND.contains(landkode)) {
            return TYPE_NORDEN;
        } else {
            return TYPE_IKKE_EOS;
        }
    }
}
