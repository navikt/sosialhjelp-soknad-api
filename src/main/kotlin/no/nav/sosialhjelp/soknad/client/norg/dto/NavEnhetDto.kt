package no.nav.sosialhjelp.soknad.client.norg.dto

import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.navenhet.NavEnhet

data class NavEnhetDto(
    val navn: String,
    val enhetNr: String,
)

fun NavEnhetDto.toNavEnhet(gt: String): NavEnhet {
    return NavEnhet(
        enhetNr = enhetNr,
        navn = navn,
        kommunenavn = null,
        sosialOrgNr = getSosialOrgNr(this, gt)
    )
}

private fun getSosialOrgNr(navEnhetDto: NavEnhetDto, gt: String): String {
    return when {
        navEnhetDto.enhetNr == "0513" && gt == "3434" -> {
            /*
                Jira sak 1200

                Lom og Skjåk har samme enhetsnummer. Derfor vil alle søknader bli sendt til Skjåk når vi henter organisajonsnummer basert på enhetNr.
                Dette er en midlertidig fix for å få denne casen til å fungere.
                */
            "974592274"
        }
        navEnhetDto.enhetNr == "0511" && gt == "3432" -> "964949204"
        navEnhetDto.enhetNr == "1620" && gt == "5014" -> "913071751"
        else -> KommuneTilNavEnhetMapper.getOrganisasjonsnummer(navEnhetDto.enhetNr)
    }
}
