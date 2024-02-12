package no.nav.sosialhjelp.soknad.adresseforslag.domain

data class VegadresseResult(
    val matrikkelId: String?,
    val adressenavn: String?,
    val husnummer: Int?,
    val husbokstav: String?,
    val postnummer: String?,
    val poststed: String?,
    val kommunenavn: String?,
    val kommunenummer: String?,
    val bydelsnavn: String?,
    val bydelsnummer: String?
) {
    val geografiskTilknytning: String
        get() {
            return when {
                bydelsnummer?.isNotEmpty() == true -> bydelsnummer
                kommunenummer?.isNotEmpty() == true -> kommunenummer
                else -> error("Geografisk tilknytning mangler")
            }
        }
}

data class MatrikkeladresseResult(
    val matrikkelId: String?,
    val tilleggsnavn: String?,
    val kommunenummer: String?,
    val gaardsnummer: String?,
    val bruksnummer: String?,
    val postnummer: String?,
    val poststed: String?
)

data class CompletionAdresse(
    val vegadresse: VegadresseResult?,
    val matrikkeladresse: MatrikkeladresseResult?
)

data class AdresseCompletionData(
    val forslagAdresse: AdresseCompletionResult?
)

data class AdresseCompletionResult(
    val suggestions: List<String> = emptyList(),
    val addressFound: CompletionAdresse?
)

data class AdresseForslagParameters(
    val completionField: String,
    val maxSuggestions: Int?,
    val fieldValues: List<CompletionFieldValue>
)

data class CompletionFieldValue(
    val fieldName: String,
    val fieldValue: String?
)
