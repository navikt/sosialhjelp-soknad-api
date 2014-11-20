package no.nav.sbl.dialogarena.kodeverk;

import java.util.List;

import static java.util.Arrays.asList;

public interface Kodeverk extends Adressekodeverk {

    String  LANDKODE        = "Landkoder",
            POSTNUMMER      = "Postnummer";
            
    List<String> ALLE_KODEVERK = asList(LANDKODE, POSTNUMMER);

    void lastInnNyeKodeverk();

    String getLandkode(String landnavn);

    List<String> getAlleLandkoder();
}
	