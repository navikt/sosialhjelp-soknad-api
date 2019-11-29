package no.nav.sbl.dialogarena.kodeverk;

import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.kodeverk.Kodeverk.EksponertKodeverk.*;

public interface Kodeverk extends Adressekodeverk {
    enum EksponertKodeverk {
        LANDKODE("Landkoder"),
        KOMMUNE("Kommuner"),
        POSTNUMMER("Postnummer");

        private final String value;

        EksponertKodeverk(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    List<String> ALLE_KODEVERK = asList(LANDKODE.toString(), POSTNUMMER.toString(), KOMMUNE.toString());

    void lastInnNyeKodeverk();

    String gjettKommunenummer(String kommunenavn);

    String getKommunenavn(final String kommunenummer);
}
    