package no.nav.sosialhjelp.soknad.navenhet.finnadresse

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import org.springframework.stereotype.Component

@Component
class FinnAdresseService(
    private val adressesokService: AdressesokService,
    private val hentAdresseService: HentAdresseService
) {

    fun finnAdresseFraSoknad(personalia: JsonPersonalia, valg: String?): AdresseForslag? {
        val adresse = getValgtAdresse(personalia, valg)
        return adresseForslagFraPDL(adresse)
    }

    private fun getValgtAdresse(personalia: JsonPersonalia, valg: String?): JsonAdresse? {
        return when (valg) {
            "folkeregistrert" -> personalia.folkeregistrertAdresse
            "midlertidig", "soknad" -> personalia.oppholdsadresse
            else -> null
        }
    }

    private fun adresseForslagFraPDL(adresse: JsonAdresse?): AdresseForslag? {
        return if (JsonAdresse.Type.MATRIKKELADRESSE == adresse?.type) {
            hentAdresseService.hentKartverketMatrikkelAdresseForInnloggetBruker()
                ?.let {
                    AdresseForslag(
                        kommunenummer = it.kommunenummer,
                        geografiskTilknytning = it.bydelsnummer ?: it.kommunenummer,
                        type = AdresseForslagType.MATRIKKELADRESSE
                    )
                }
        } else if (JsonAdresse.Type.GATEADRESSE == adresse?.type) {
            adressesokService.getAdresseForslag(adresse as JsonGateAdresse)
        } else {
            null
        }
    }
}
