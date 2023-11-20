package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

interface OkonomiType

enum class GenerellOkonomiType(tittel: String = "") : OkonomiType {
    SKATTEMELDING_SKATTEMELDING("skattemelding|skattemelding");
}
