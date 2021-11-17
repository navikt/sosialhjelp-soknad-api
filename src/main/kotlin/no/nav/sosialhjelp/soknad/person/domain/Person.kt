package no.nav.sosialhjelp.soknad.person.domain

data class Person(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fnr: String,
    val sivilstatus: String?,
    val statsborgerskap: List<String>?,
    var ektefelle: Ektefelle?,
    val bostedsadresse: Bostedsadresse?,
    val oppholdsadresse: Oppholdsadresse?,
    val kontaktadresse: Kontaktadresse?
)
