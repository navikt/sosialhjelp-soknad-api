package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService.EOS_LAND;

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
