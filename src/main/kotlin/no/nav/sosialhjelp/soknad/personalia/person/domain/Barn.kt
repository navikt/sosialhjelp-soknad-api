package no.nav.sosialhjelp.soknad.personalia.person.domain

import java.time.LocalDate

data class Barn(
    val fnr: String,
    val fodselsdato: LocalDate?,
    val folkeregistrertSammen: Boolean,
)
