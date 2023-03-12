package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

import com.fasterxml.jackson.annotation.JsonValue

enum class VedleggGruppe(
    @JsonValue val stringName: String
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
