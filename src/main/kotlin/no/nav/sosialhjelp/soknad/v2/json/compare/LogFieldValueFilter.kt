package no.nav.sosialhjelp.soknad.v2.json.compare

/**
 * En oversikt over felter hvor verdien kan skrives ut til logg.
 * For enklere å kunne se feil.
 */
object LogFieldValueFilter {

    // TODO Egentlig noe poeng å logge ut verdier for noenting når det er snakk om så lite hjelp uansett?
    private val noFilterFields = listOf(
        "[Aa]dresse.type",
        ".hendelseReferanse",
        ".sha512",
        ".kilde",
        ".overstyrtAvBruker",
        "soknad.driftsinformasjon",
        ".navEnhetsnavn",
        "mottaker.organisasjonsnummer",

    )

    fun isNotFiltered(field: String): Boolean {
        return noFilterFields.any { field.contains(it.toRegex()) }
    }
}

enum class LoggerComparisonErrorTypes(private val logString: String) {
    FIELD_FAILURE("** FieldFailure **"),
    MISSING_FIELD("** MissingField **"),
    ARRAY_SIZE("** ArraySize **");

    override fun toString(): String {
        return logString
    }
}
