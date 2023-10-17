package no.nav.sosialhjelp.soknad.model

import java.time.LocalDateTime
import java.util.*

data class NySoknadDto (
    val soknadId: UUID
)
data class SoknadDto (
    val soknadId: UUID,
    val innsendingsTidspunkt: LocalDateTime? = null
)
data class BegrunnelseDto (
    val hvaSokesOm: String?,
    val hvorforSoke: String?
)
data class BosituasjonDto (
    val botype: Botype?,
    val antallPersoner: Int
)
data class ArbeidDto (
    val kommentarArbeid: String?,
    val arbeidsforhold: List<ArbeidsforholdDto>?
)

data class ArbeidsforholdDto (
    val arbeidsgivernavn: String,
    val orgnummer: String?,
    val fraOgMed: String?,
    val tilOgMed: String?,
    val stillingsprosent: Int?,
    val stillingstype: Stillingstype?
)