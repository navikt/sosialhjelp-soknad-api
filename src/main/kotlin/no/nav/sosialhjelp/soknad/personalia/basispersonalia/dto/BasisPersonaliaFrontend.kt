package no.nav.sosialhjelp.soknad.personalia.basispersonalia.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.StringUtils.isEmpty

@Schema(nullable = true)
data class BasisPersonaliaFrontend(
    var navn: NavnFrontend? = null,
    var fodselsnummer: String? = null,
    var statsborgerskap: String? = null,
    var nordiskBorger: Boolean? = null
)

data class NavnFrontend(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null
) {
    val fulltNavn: String
        get() {
            val f = if (!isEmpty(fornavn)) fornavn else ""
            val m = if (!isEmpty(mellomnavn)) " $mellomnavn" else ""
            val e = if (!isEmpty(etternavn)) " $etternavn" else ""
            return (f + m + e).trim { it <= ' ' }
        }
}
