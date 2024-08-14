package no.nav.sosialhjelp.soknad.v2.navn

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn

data class Navn(
    val fornavn: String,
    val mellomnavn: String = "",
    val etternavn: String,
)

fun Navn.toJson() = JsonNavn().withFornavn(fornavn).withMellomnavn(mellomnavn).withEtternavn(etternavn)
