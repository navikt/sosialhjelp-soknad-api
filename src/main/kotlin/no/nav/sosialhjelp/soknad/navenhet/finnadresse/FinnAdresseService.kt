package no.nav.sosialhjelp.soknad.navenhet.finnadresse

import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import org.springframework.stereotype.Component

@Component
class FinnAdresseService(
    private val adressesokService: AdressesokService,
    private val hentAdresseService: HentAdresseService,
) {
    fun finnAdresseFraSoknad(
        adresse: Adresse,
    ): AdresseForslag? = adresseForslagFraPDL(adresse)

    private fun adresseForslagFraPDL(adresse: Adresse?): AdresseForslag? =
        when (adresse) {
            is MatrikkelAdresse -> hentMatrikkeladresse()
            is VegAdresse -> adressesokService.getAdresseForslag(adresse)
            else -> null
        }

    private fun hentMatrikkeladresse() =
        hentAdresseService
            .hentKartverketMatrikkelAdresseForInnloggetBruker()
            ?.let {
                AdresseForslag(
                    kommunenummer = it.kommunenummer,
                    geografiskTilknytning = it.bydelsnummer ?: it.kommunenummer,
                    type = AdresseForslagType.MATRIKKELADRESSE,
                )
            }
}
