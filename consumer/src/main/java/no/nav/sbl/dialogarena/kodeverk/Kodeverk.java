package no.nav.sbl.dialogarena.kodeverk;

import java.util.Arrays;
import java.util.List;

public interface Kodeverk extends Adressekodeverk {

    String  LANDKODE        = "Landkoder",
            POSTNUMMER      = "Postnummer";
            
    List<String> ALLE_KODEVERK = Arrays.asList(LANDKODE, POSTNUMMER);

    void lastInnNyeKodeverk();

    String getLandkode(String landnavn);

    List<String> getAlleLandkoder();
}
