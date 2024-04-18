package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonTilleggsinformasjon(
    val enhetsnummer: String?,
)
