package no.nav.sosialhjelp.soknad.v2.json.generate

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import java.util.UUID

interface DomainToJsonMapper {
    fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    )
}
