package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService;

public class StatsborgerskapType {

    public static String get(String landkode) {
        if ("NOR".equals(landkode)) {
            return "norsk";
        } else if (LandService.EOS_LAND.contains(landkode)) {
            return "eos";
        } else {
            return "ikkeEos";
        }
    }
}
