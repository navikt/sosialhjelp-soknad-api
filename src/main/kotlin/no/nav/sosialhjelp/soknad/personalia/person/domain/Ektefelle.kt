package no.nav.sosialhjelp.soknad.personalia.person.domain

import java.io.Serializable
import java.time.LocalDate

data class Ektefelle(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val fodselsdato: LocalDate?,
    val fnr: String?,
    val folkeregistrertSammen: Boolean,
    val ikkeTilgangTilEktefelle: Boolean
) : Serializable {
    constructor(ikkeTilgangTilEktefelle: Boolean) : this(null, null, null, null, null, false, ikkeTilgangTilEktefelle)
}
