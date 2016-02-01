package no.nav.sbl.dialogarena.kodeverk;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.kodeverk.Kodeverk.EksponertKodeverk.*;

public interface Kodeverk extends Adressekodeverk {
    public enum EksponertKodeverk {
        LANDKODE("Landkoder"),
        KOMMUNE("Kommuner"),
        POSTNUMMER("Postnummer");

        private final String value;

        private EksponertKodeverk(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    List<String> ALLE_KODEVERK = asList(LANDKODE.toString(), POSTNUMMER.toString(), KOMMUNE.toString());

    void lastInnNyeKodeverk();

    public List<String> hentAlleKodenavnFraKodeverk(EksponertKodeverk kodeverknavn);

    public Map<String, String> hentAlleKodenavnMedForsteTerm(EksponertKodeverk kodeverknavn);
}
	