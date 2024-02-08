package no.nav.sosialhjelp.soknad.adressesok.sok

import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService

object AdresseStringSplitter {

    fun toSokedata(kodeverkService: KodeverkService?, adresse: String): Sokedata? {
        return if (isAdressTooShort(adresse)) {
            Sokedata(adresse = adresse)
        } else firstNonNull(
            fullstendigGateadresseMatch(kodeverkService, adresse),
            Sokedata(adresse = adresse)
        )
    }

    /**
     * Trimmer en navngitt MatchGroup og returnerer null hvis resultatet er en tom streng, strengen hvis ikke.
     */
    private fun MatchResult.asTrimmedOrNull(group: String): String? = this.groups[group]?.value?.trim()?.takeUnless { it.isEmpty() }

    /**
     * Erstatter alle forekomster av 1 eller flere whitespace med et enkelt mellomrom.
     */
    private fun String.deduplicateWhitespace(): String = this.replace(Regex("\\s+"), " ")

    private val adresseRegex = Regex(
        """(?x) # Enable comments and ignore whitespace in the regex pattern
            ^(?<adresse>[^0-9,]*\b*)? # Optionally capture the street name, allowing letters and spaces
            (?<husnummer>\s*\d*)? # Optionally capture the street number, allowing leading spaces
            (?<husbokstav>\s*[A-Za-z]?)? # Optionally capture the optional letter, allowing leading spaces
            ([,\s]*(?<postnummer>\d{0,4})\s*)? # Optionally capture the 4-digit postal code, allowing for partial entry
            (?<poststed>[A-Za-zÆØÅæøå\s]*)? # Optionally capture the city name, allowing letters and spaces     
        """.trimIndent()
    )

    private fun fullstendigGateadresseMatch(kodeverkService: KodeverkService?, adresse: String): Sokedata? =
        adresseRegex.matchEntire(adresse.deduplicateWhitespace())?.let { matchResult ->
            val gateAdresse = matchResult.asTrimmedOrNull("adresse")
            val husnummer = matchResult.asTrimmedOrNull("husnummer")
            val husbokstav = matchResult.asTrimmedOrNull("husbokstav")
            val postnummer = matchResult.asTrimmedOrNull("postnummer")
            val poststed = matchResult.asTrimmedOrNull("poststed")
            val kommunenummer = getKommunenummer(kodeverkService, poststed)

            Sokedata(gateAdresse, husnummer, husbokstav, postnummer, poststed, kommunenummer)
        }

    private fun getKommunenummer(kodeverkService: KodeverkService?, kommunenavn: String?): String? {
        return if (kommunenavn != null && kommunenavn.trim { it <= ' ' }.isNotEmpty() && kodeverkService != null) {
            kodeverkService.gjettKommunenummer(kommunenavn)
        } else null
    }

    private fun firstNonNull(vararg elems: Sokedata?): Sokedata? {
        return elems.firstOrNull { it != null }
    }

    fun isAdressTooShort(address: String): Boolean {
        return address.trim { it <= ' ' }.length < 2
    }
}
