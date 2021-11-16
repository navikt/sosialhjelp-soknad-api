package no.nav.sosialhjelp.soknad.navenhet.gt.dto

data class HentGeografiskTilknytning(
    val hentGeografiskTilknytning: GeografiskTilknytningDto
)

data class GeografiskTilknytningDto(
    val gtType: GtType,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtLand: String?
)

enum class GtType {
    BYDEL, KOMMUNE, UDEFINERT, UTLAND
}
