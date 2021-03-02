package no.nav.sosialhjelp.soknad.domain.model.util;

import static no.nav.sosialhjelp.soknad.domain.model.util.LandListe.EOS_LAND;

public final class StatsborgerskapType {

    private StatsborgerskapType() {
    }

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
