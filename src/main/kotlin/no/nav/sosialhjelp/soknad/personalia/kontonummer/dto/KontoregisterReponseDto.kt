package no.nav.sosialhjelp.soknad.personalia.kontonummer.dto

data class KontoDto(
    val kontonummer: String,
    val utenlandsKontoInfo: UtenlandskKontoInfo?
)

data class UtenlandskKontoInfo(
    val banknavn: String?,
    val bankkode: String?,
    val bankLandkode: String,
    val valutakode: String,
    val swiftBicKode: String?,
    val bankadresse1: String?,
    val bankadresse2: String?,
    val bankadresse3: String?
)
