package no.nav.sosialhjelp.soknad.navenhet.bydel

data class BydelFordeling(
    val veiadresse: String,
    val gatekode: String,
    val husnummerfordeling: List<Husnummerfordeling>,
    val bydelFra: String,
    val bydelTil: String,
    val bydelsnavnTil: String,
)

data class Husnummerfordeling(
    val fra: Int,
    val til: Int,
    val type: HusnummerfordelingType,
)

enum class HusnummerfordelingType {
    ODD,
    EVEN,
    ALL,
}
