package no.nav.sosialhjelp.soknad.api.informasjon.dto

import java.util.Collections

data class KommunestatusFrontend(
    var kommunenummer: String,
    var kanMottaSoknader: Boolean,
    var kanOppdatereStatus: Boolean,
    var harMidlertidigDeaktivertMottak: Boolean = false,
    var harMidlertidigDeaktivertOppdateringer: Boolean = false,
    var harNksTilgang: Boolean = false,
    var behandlingsansvarlig: String? = null,
    var kontaktPersoner: KontaktPersonerFrontend? = null
)

data class KontaktPersonerFrontend(
    val fagansvarligEpost: Collection<String> = Collections.emptyList(),
    var tekniskAnsvarligEpost: Collection<String> = Collections.emptyList()
)
