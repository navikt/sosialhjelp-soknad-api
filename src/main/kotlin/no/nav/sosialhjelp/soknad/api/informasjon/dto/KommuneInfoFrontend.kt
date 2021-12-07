package no.nav.sosialhjelp.soknad.api.informasjon.dto

data class KommuneInfoFrontend(
    var kommunenummer: String,
    var kanMottaSoknader: Boolean,
    var kanOppdatereStatus: Boolean
)
