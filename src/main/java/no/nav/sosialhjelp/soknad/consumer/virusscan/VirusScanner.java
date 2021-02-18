package no.nav.sosialhjelp.soknad.consumer.virusscan;

import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException;

/**
 * Integrasjonen er kopiert fra https://github.com/navikt/foreldrepengesoknad-api og modifisert til eget bruk
 */
public interface VirusScanner {
    void scan(String filnavn, byte[] data, String behandlingsId, String fileType) throws OpplastingException;
}
