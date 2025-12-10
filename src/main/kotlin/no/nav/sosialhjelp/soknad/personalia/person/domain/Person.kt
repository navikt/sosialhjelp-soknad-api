package no.nav.sosialhjelp.soknad.personalia.person.domain

import java.time.LocalDate

data class Person(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fnr: String,
    val fodselsdato: LocalDate?,
    val sivilstatus: String?,
    val statsborgerskap: List<String>?,
    var ektefelle: Ektefelle?,
    val bostedsadresse: Bostedsadresse?,
    val oppholdsadresse: Oppholdsadresse?,
)
