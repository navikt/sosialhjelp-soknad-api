package no.nav.sosialhjelp.soknad.nymodell.controller.dto

import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Stillingstype
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Studentgrad

data class BosituasjonDto(
    val botype: Botype?,
    val antallPersoner: Int
)

data class ArbeidRequest(
    val kommentarArbeid: String
)

data class ArbeidResponse(
    val kommentarArbeid: String?,
    val arbeidsforhold: List<ArbeidsforholdDto>
)

data class ArbeidsforholdDto(
    val arbeidsgivernavn: String?,
    val fraOgMed: String? = null,
    val tilOgMed: String? = null,
    val stillingsprosent: Int? = null,
    val stillingstype: Stillingstype? = null
)

data class UtdanningDTO(
    val erStudent: Boolean?,
    val studentgrad: Studentgrad?
)
