package no.nav.sosialhjelp.soknad.health.selftest.generators

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.sosialhjelp.soknad.health.selftest.Selftest

/*
Kopiert inn fra no.nav.sbl.dialogarena:common-web
Endringer gjort i no.nav.common:web gjør at vi heller benytter den fra det gamle artefaktet.
Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
*/
object SelftestJsonGenerator {
    fun generate(selftest: Selftest?): String {
        val om = ObjectMapper()
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return om.writeValueAsString(selftest)
    }
}
