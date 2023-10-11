package no.nav.sosialhjelp.soknad.model

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype

data class BosituasjonDTO (
    val botype: Botype?,
    val antallPersoner: Int?
)