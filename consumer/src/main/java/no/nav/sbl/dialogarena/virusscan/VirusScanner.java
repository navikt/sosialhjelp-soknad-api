package no.nav.sbl.dialogarena.virusscan;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;

/**
 * Integrasjonen er kopiert fra https://github.com/navikt/foreldrepengesoknad-api og modifisert til eget bruk
 */
public interface VirusScanner {
    void scan(String filnavn, byte[] data, String behandlingsId, String mimeType) throws OpplastingException;
}
