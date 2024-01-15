package no.nav.sosialhjelp.soknad.oppsummering.dto

data class Oppsummering(
    val steg: List<Steg>,
)

data class Steg(
    val stegNr: Int,
    val tittel: String,
    val avsnitt: List<Avsnitt>,
)

data class Avsnitt(
    val tittel: String,
    val sporsmal: List<Sporsmal>,
)

data class Sporsmal(
    val tittel: String?,
    val felt: List<Felt>?,
    val erUtfylt: Boolean,
) {
    fun containsFeltWithSvar(svar: String): Boolean {
        return felt != null && felt.any { svar == it.svar?.value }
    }
}

data class Felt(
    val label: String? = null,
    val svar: Svar? = null,
    val labelSvarMap: Map<String, Svar>? = null,
    val type: Type,
    val vedlegg: List<Vedlegg>? = null,
)

data class Svar(
    val value: String?,
    val type: SvarType,
)

data class Vedlegg(
    val filnavn: String,
    val uuid: String?,
)

enum class Type {
    TEKST, CHECKBOX, SYSTEMDATA, SYSTEMDATA_MAP, VEDLEGG
}

enum class SvarType {
    LOCALE_TEKST, TEKST, DATO, TIDSPUNKT
}
