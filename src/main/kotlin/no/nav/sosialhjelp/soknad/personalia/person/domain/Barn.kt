package no.nav.sosialhjelp.soknad.personalia.person.domain

import java.time.LocalDate

data class Barn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fnr: String,
    val fodselsdato: LocalDate?,
    val folkeregistrertSammen: Boolean
)
