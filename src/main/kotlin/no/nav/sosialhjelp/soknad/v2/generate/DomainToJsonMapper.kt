package no.nav.sosialhjelp.soknad.v2.generate

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import java.util.*

interface DomainToJsonMapper {
    fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad)
}
