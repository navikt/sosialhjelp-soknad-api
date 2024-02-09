package no.nav.sosialhjelp.soknad.adressesok

import org.springframework.stereotype.Component

@Component
class AdresseforslagService(
    private val adresseforslagClient: AdresseforslagClient,
) {
    /**
     * Minimum string length before search is actually performed
     */
    private val MINIMUM_ADRESS_SEARCH_LENGTH = 3

    fun find(substring: String): Any? {
        if (substring.length <= MINIMUM_ADRESS_SEARCH_LENGTH) return null

        return adresseforslagClient.getAdresseforslag(substring).block()
    }
}
