package no.nav.sosialhjelp.soknad.navenhet.domain

import no.nav.sosialhjelp.soknad.common.mapper.KommuneTilNavEnhetMapper

data class NavEnhet(
    val enhetNr: String?,
    val navn: String,
    val kommunenavn: String?,
    val sosialOrgNr: String?
)

data class NavEnheterFraLokalListe(
    val navenheter: List<NavEnhetFraLokalListe>?
)

data class NavEnhetFraLokalListe(
    val kommunenummer: String,
    val kommunenavn: String,
    val geografiskTilknytning: String,
    val enhetsnummer: String,
    val enhetsnavn: String
)

fun NavEnhetFraLokalListe.toNavEnhet(): NavEnhet {
    return NavEnhet(
        enhetNr = enhetsnummer,
        navn = enhetsnavn,
        kommunenavn = kommunenavn,
        sosialOrgNr = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(enhetsnummer)
    )
}
