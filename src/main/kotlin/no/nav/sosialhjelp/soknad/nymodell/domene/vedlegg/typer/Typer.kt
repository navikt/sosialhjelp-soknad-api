package no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.typer

enum class VedleggStatus {
    KREVES, LASTET_OPP, LEVERT
}

enum class VedleggHendelseType(value: String?) {
    DOKUMENTASJON_ETTERSPURT("dokumentasjonEtterspurt"),
    DOKUMENTASJONKRAV("dokumentasjonkrav"),
    SOKNAD("soknad"),
    BRUKER("bruker");
}
