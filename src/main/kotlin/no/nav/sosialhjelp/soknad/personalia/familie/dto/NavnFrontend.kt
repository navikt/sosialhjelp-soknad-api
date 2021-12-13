package no.nav.sosialhjelp.soknad.personalia.familie.dto

import org.apache.commons.lang3.StringUtils

data class NavnFrontend(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null
) {
    val fulltNavn: String
        get() {
            val f = if (!StringUtils.isEmpty(fornavn)) fornavn else ""
            val m = if (!StringUtils.isEmpty(mellomnavn)) " $mellomnavn" else ""
            val e = if (!StringUtils.isEmpty(etternavn)) " $etternavn" else ""
            return (f + m + e).trim { it <= ' ' }
        }
}
