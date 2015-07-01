package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService.EOS_LAND;

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
