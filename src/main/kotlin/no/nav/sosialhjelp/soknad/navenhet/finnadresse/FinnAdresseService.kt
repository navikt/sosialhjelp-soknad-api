package no.nav.sosialhjelp.soknad.navenhet.finnadresse

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.stereotype.Component

@Component
open class FinnAdresseService(
    private val adressesokService: AdressesokService,
    private val personService: PersonService,
    private val hentAdresseService: HentAdresseService
) {

    open fun finnAdresseFraSoknad(personalia: JsonPersonalia, valg: String?): AdresseForslag? {
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
            adresseForslagForMatrikkelAdresse()
        } else if (JsonAdresse.Type.GATEADRESSE == adresse?.type) {
            adressesokService.getAdresseForslag(adresse as JsonGateAdresse)
        } else {
            null
        }
    }

    /**
     * Hvis matrikkelAdresse -> kall hentPerson fra PDL og bruk matrikkelId til hentAdresse
     * for å hente matrikkeladresse med kommunenummer og evt bydelsnummer.
     *
     * Matrikkeladresser er kun relevant ved valg av folkeregistrert adresse, ettersom vårt adressesøk ikke
     * støtter matrikkeladresser, og matrikkeladresse som oppholdsadresse i PDL ikke finnes.
     */
    private fun adresseForslagForMatrikkelAdresse(): AdresseForslag? {
        val person = personService.hentPerson(getUserIdFromToken())
        val matrikkelAdresse = person?.bostedsadresse?.matrikkeladresse?.matrikkelId?.let {
            hentAdresseService.hentKartverketMatrikkelAdresse(it)
        } ?: return null
        return AdresseForslag(
            kommunenummer = matrikkelAdresse.kommunenummer,
            geografiskTilknytning = matrikkelAdresse.bydelsnummer ?: matrikkelAdresse.kommunenummer,
            type = AdresseForslagType.MATRIKKELADRESSE
        )
    }
}
