package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend

object PersonMapper {

    fun getPersonnummerFromFnr(fnr: String?): String? {
        return fnr?.substring(6)
    }

    fun mapToJsonNavn(navn: NavnFrontend?): JsonNavn? {
        return if (navn == null) {
            null
        } else JsonNavn()
            .withFornavn(navn.fornavn ?: "")
            .withMellomnavn(navn.mellomnavn ?: "")
            .withEtternavn(navn.etternavn ?: "")
    }
}
