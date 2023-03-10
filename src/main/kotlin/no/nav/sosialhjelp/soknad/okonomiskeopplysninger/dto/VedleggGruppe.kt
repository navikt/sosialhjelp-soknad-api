package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

enum class VedleggGruppe(
    private val stringName: String
) {
    AndreUtgifter("andre utgifter"),
    Arbeid("arbeid"),
    Bosituasjon("bosituasjon"),
    Familie("familie"),
    GenerelleVedlegg("generelle vedlegg"),
    Inntekt("inntekt"),
    Statsborgerskap("statsborgerskap"),
    Utgifter("utgifter");

    override fun toString(): String {
        return stringName
    }
}
