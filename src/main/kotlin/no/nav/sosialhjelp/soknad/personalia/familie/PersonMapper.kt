package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import org.apache.commons.lang3.StringUtils

object PersonMapper {

    fun getPersonnummerFromFnr(fnr: String?): String? {
        return fnr?.substring(6)
    }

    fun mapToJsonNavn(navn: NavnFrontend?): JsonNavn? {
        return if (navn == null) {
            null
        } else {
            JsonNavn()
                .withFornavn(navn.fornavn ?: "")
                .withMellomnavn(navn.mellomnavn ?: "")
                .withEtternavn(navn.etternavn ?: "")
        }
    }

    fun fulltNavn(navn: JsonNavn): String {
        val f = if (!StringUtils.isEmpty(navn.fornavn)) navn.fornavn else ""
        val m = if (!StringUtils.isEmpty(navn.mellomnavn)) " ${navn.mellomnavn}" else ""
        val e = if (!StringUtils.isEmpty(navn.etternavn)) " ${navn.etternavn}" else ""
        return (f + m + e).trim { it <= ' ' }
    }
}
