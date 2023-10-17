package no.nav.sosialhjelp.soknad.model

import org.springframework.data.annotation.Id

data class PersonIdentifikator (
    val verdi: String
)

data class Person (
    @Id val id: PersonIdentifikator,
    val fornavn: String,
    val mellomnavn: String,
    val etternavn: String,
    val statsborgerskap: String,
    val nordiskBorger: Boolean,
)

//data class Adresse (
//
//
//
//)
//
//data class Telefonnummer (
//
//)