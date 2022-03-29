package no.nav.sosialhjelp.soknad.navenhet.finnadresse

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import org.springframework.stereotype.Component

@Component
open class FinnAdresseService(
    private val adressesokService: AdressesokService
) {

    open fun finnAdresseFraSoknad(personalia: JsonPersonalia, valg: String?): List<AdresseForslag> {
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

    private fun adresseForslagFraPDL(adresse: JsonAdresse?): List<AdresseForslag> {
        if (JsonAdresse.Type.MATRIKKELADRESSE == adresse?.type) {
            return adresseForslagForMatrikkelAdresse(adresse as JsonMatrikkelAdresse)
        } else if (JsonAdresse.Type.GATEADRESSE == adresse?.type) {
            val adresseForslag = adressesokService.getAdresseForslag(adresse as JsonGateAdresse)
            return listOf(adresseForslag)
        }
        return emptyList()
    }

    private fun adresseForslagForMatrikkelAdresse(adresse: JsonMatrikkelAdresse): List<AdresseForslag> {
        val kommunenummer = adresse.kommunenummer
        if (kommunenummer == null || kommunenummer.trim { it <= ' ' } == "") {
            return emptyList()
        }
        return listOf(AdresseForslag(kommunenummer, AdresseForslagType.MATRIKKELADRESSE))
    }
}
