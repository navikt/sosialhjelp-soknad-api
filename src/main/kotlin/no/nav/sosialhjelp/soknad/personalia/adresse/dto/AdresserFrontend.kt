package no.nav.sosialhjelp.soknad.personalia.adresse.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend

data class AdresserFrontendInput(
    val valg: JsonAdresseValg?,
    val folkeregistrert: AdresseFrontend? = null,
    val midlertidig: AdresseFrontend? = null,
    val soknad: AdresseFrontend? = null,
)

data class AdresserFrontend(
    val valg: JsonAdresseValg?,
    val folkeregistrert: AdresseFrontend? = null,
    val midlertidig: AdresseFrontend? = null,
    val soknad: AdresseFrontend? = null,
    val navEnhet: NavEnhetFrontend? = null
)

@Schema(nullable = true)
data class AdresseFrontend(
    var type: JsonAdresse.Type? = null,
    var gateadresse: GateadresseFrontend? = null,
    var matrikkeladresse: MatrikkeladresseFrontend? = null,
    var ustrukturert: UstrukturertAdresseFrontend? = null
)

data class GateadresseFrontend(
    val landkode: String? = null,
    val kommunenummer: String? = null,
    val adresselinjer: List<String>? = null,
    val bolignummer: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val gatenavn: String? = null,
    val husnummer: String? = null,
    val husbokstav: String? = null,
)

data class MatrikkeladresseFrontend(
    val kommunenummer: String? = null,
    val gaardsnummer: String? = null,
    val bruksnummer: String? = null,
    val festenummer: String? = null,
    val seksjonsnummer: String? = null,
    val undernummer: String? = null,
)

data class UstrukturertAdresseFrontend(
    val adresse: List<String>? = null
)
