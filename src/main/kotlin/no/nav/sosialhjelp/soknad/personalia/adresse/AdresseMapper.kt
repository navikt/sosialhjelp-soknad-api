package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.GateadresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.MatrikkeladresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.UstrukturertAdresseFrontend
import org.apache.commons.lang3.StringUtils

object AdresseMapper {

    fun mapToAdresserFrontend(
        sysFolkeregistrert: JsonAdresse?,
        sysMidlertidig: JsonAdresse?,
        jsonOpphold: JsonAdresse?,
        navEnhet: NavEnhetFrontend?,
    ): AdresserFrontend {
        return AdresserFrontend(
            valg = jsonOpphold?.adresseValg,
            folkeregistrert = mapToAdresseFrontend(sysFolkeregistrert),
            midlertidig = mapToAdresseFrontend(sysMidlertidig),
            soknad = mapToAdresseFrontend(jsonOpphold),
            navEnhet = navEnhet,
        )
    }

    private fun mapToAdresseFrontend(adresse: JsonAdresse?): AdresseFrontend? {
        if (adresse == null) {
            return null
        }
        return when (adresse.type) {
            JsonAdresse.Type.GATEADRESSE -> {
                AdresseFrontend(
                    type = JsonAdresse.Type.GATEADRESSE,
                    gateadresse = mapToGateadresseFrontend(adresse),
                )
            }
            JsonAdresse.Type.MATRIKKELADRESSE -> {
                AdresseFrontend(
                    type = JsonAdresse.Type.MATRIKKELADRESSE,
                    matrikkeladresse = mapToMatrikkeladresseFrontend(adresse),
                )
            }
            JsonAdresse.Type.USTRUKTURERT -> {
                AdresseFrontend(
                    type = JsonAdresse.Type.USTRUKTURERT,
                    ustrukturert = mapToUstrukturertAdresseFrontend(adresse),
                )
            }
            else -> null
        }
    }

    private fun mapToGateadresseFrontend(adresse: JsonAdresse): GateadresseFrontend {
        val gateAdresse = adresse as JsonGateAdresse
        return GateadresseFrontend(
            landkode = gateAdresse.landkode,
            kommunenummer = gateAdresse.kommunenummer,
            adresselinjer = gateAdresse.adresselinjer,
            bolignummer = gateAdresse.bolignummer,
            postnummer = gateAdresse.postnummer,
            poststed = gateAdresse.poststed,
            gatenavn = gateAdresse.gatenavn,
            husnummer = gateAdresse.husnummer,
            husbokstav = gateAdresse.husbokstav,
        )
    }

    private fun mapToMatrikkeladresseFrontend(adresse: JsonAdresse): MatrikkeladresseFrontend {
        val matrikkelAdresse = adresse as JsonMatrikkelAdresse
        return MatrikkeladresseFrontend(
            kommunenummer = matrikkelAdresse.kommunenummer,
            gaardsnummer = matrikkelAdresse.gaardsnummer,
            bruksnummer = matrikkelAdresse.bruksnummer,
            festenummer = matrikkelAdresse.festenummer,
            seksjonsnummer = matrikkelAdresse.seksjonsnummer,
            undernummer = matrikkelAdresse.undernummer,
        )
    }

    private fun mapToUstrukturertAdresseFrontend(adresse: JsonAdresse): UstrukturertAdresseFrontend {
        val ustrukturertAdresse = adresse as JsonUstrukturertAdresse
        return UstrukturertAdresseFrontend(adresse = ustrukturertAdresse.adresse)
    }

    fun mapToJsonAdresse(adresseFrontend: AdresseFrontend): JsonAdresse {
        val adresse: JsonAdresse = when (adresseFrontend.type) {
            JsonAdresse.Type.GATEADRESSE -> {
                val gateadresse = adresseFrontend.gateadresse
                JsonGateAdresse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(JsonAdresse.Type.GATEADRESSE)
                    .withLandkode(StringUtils.defaultIfBlank(gateadresse?.landkode, "NOR"))
                    .withKommunenummer(StringUtils.defaultIfBlank(gateadresse?.kommunenummer, null))
                    .withAdresselinjer(gateadresse?.adresselinjer)
                    .withBolignummer(StringUtils.defaultIfBlank(gateadresse?.bolignummer, null))
                    .withPostnummer(StringUtils.defaultIfBlank(gateadresse?.postnummer, null))
                    .withPoststed(StringUtils.defaultIfBlank(gateadresse?.poststed, null))
                    .withGatenavn(StringUtils.defaultIfBlank(gateadresse?.gatenavn, null))
                    .withHusnummer(StringUtils.defaultIfBlank(gateadresse?.husnummer, null))
                    .withHusbokstav(StringUtils.defaultIfBlank(gateadresse?.husbokstav, null))
            }
            JsonAdresse.Type.MATRIKKELADRESSE -> {
                val matrikkeladresse = adresseFrontend.matrikkeladresse
                JsonMatrikkelAdresse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(JsonAdresse.Type.MATRIKKELADRESSE)
                    .withKommunenummer(StringUtils.defaultIfBlank(matrikkeladresse?.kommunenummer, null))
                    .withGaardsnummer(StringUtils.defaultIfBlank(matrikkeladresse?.gaardsnummer, null))
                    .withBruksnummer(StringUtils.defaultIfBlank(matrikkeladresse?.bruksnummer, null))
                    .withFestenummer(StringUtils.defaultIfBlank(matrikkeladresse?.festenummer, null))
                    .withSeksjonsnummer(StringUtils.defaultIfBlank(matrikkeladresse?.seksjonsnummer, null))
                    .withUndernummer(StringUtils.defaultIfBlank(matrikkeladresse?.undernummer, null))
            }
            else -> throw IllegalStateException("Ukjent adressetype: \"" + adresseFrontend.type + "\".")
        }
        return adresse
    }
}
