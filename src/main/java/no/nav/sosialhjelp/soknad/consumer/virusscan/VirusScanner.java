package no.nav.sosialhjelp.soknad.consumer.virusscan;


/**
 * Integrasjonen er kopiert fra https://github.com/navikt/foreldrepengesoknad-api og modifisert til eget bruk
 */
public interface VirusScanner {
    void scan(String filnavn, byte[] data, String behandlingsId, String fileType);
}
