package no.nav.sosialhjelp.soknad.adresseforslag

import no.nav.sosialhjelp.soknad.adresseforslag.domain.AdresseCompletionResult
import org.springframework.stereotype.Component

@Component
class AdresseforslagService(
    private val adresseforslagClient: AdresseforslagClient,
) {
    /**
     * Minimum string length before search is actually performed
     */
    private val MINIMUM_ADRESS_SEARCH_LENGTH = 3

    fun find(substring: String): AdresseCompletionResult? = when {
        (substring.length >= MINIMUM_ADRESS_SEARCH_LENGTH) -> null
        else -> adresseforslagClient.getAdresseforslag(substring)
    }
}
