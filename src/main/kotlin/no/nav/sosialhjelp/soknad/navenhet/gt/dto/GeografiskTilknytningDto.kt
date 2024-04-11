package no.nav.sosialhjelp.soknad.navenhet.gt.dto

data class HentGeografiskTilknytning(
    val hentGeografiskTilknytning: GeografiskTilknytningDto?
)

data class GeografiskTilknytningDto(
    val gtType: GtType,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?
)

/**
 * Sjekker om en GT er norsk, og dermed hvorvidt en fire- eller seks-sifret GT-kode kan utledes
 */
fun GeografiskTilknytningDto.erNorsk(): Boolean = gtType == GtType.KOMMUNE || gtType == GtType.BYDEL

/**
 * Henter fire- eller sekssifret GT-kode fra GeografiskTilknytningDto.
 * Returnerer gtKommune hvis gtType er KOMMUNE, gtBydel hvis gtType er BYDEL, ellers null
 */
fun GeografiskTilknytningDto.toGtStringOrThrow(): String = when (gtType) {
    GtType.BYDEL -> gtBydel ?: error("gtType er BYDEL men gtBydel er null")
    GtType.KOMMUNE -> gtKommune ?: error("gtType er KOMMUNE men gtKommune er null")
    else -> error("gtType er hverken BYDEL eller KOMMUNE")
}

enum class GtType {
    BYDEL, KOMMUNE, UDEFINERT, UTLAND
}
