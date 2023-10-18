package no.nav.sosialhjelp.soknad.model

import java.util.*

data class AdresseForSoknadId (
    val soknadId: UUID,
    val typeAdressevalg: AdresseValg
)
data class AdresseForSoknad (
    val id: AdresseForSoknadId,
    var adresseType: AdresseType,
    var adresse: AdresseObject
)
