package no.nav.sbl.dialogarena.kodeverk;

import no.nav.sbl.dialogarena.adresse.Adressekodeverk;
import no.nav.sbl.dialogarena.konto.HarValuta;
import no.nav.sbl.dialogarena.telefonnummer.HarTelefonLand;

import java.util.Arrays;
import java.util.List;

public interface Kodeverk extends Adressekodeverk, HarTelefonLand, HarValuta {

    String  LANDKODE        = "Landkoder",
            VALUTA          = "Valutaer",
            POSTNUMMER      = "Postnummer",
            RETNINGSNUMRE   = "Retningsnumre";

    List<String> ALLE_KODEVERK = Arrays.asList(LANDKODE, VALUTA, POSTNUMMER, RETNINGSNUMRE);

    void lastInnNyeKodeverk();

    String getLandkode(String landnavn);

    List<String> getAlleLandkoder();

    List<String> getAlleTelefonnummerLandkoder();

    List<String> getAlleValuta();

}
