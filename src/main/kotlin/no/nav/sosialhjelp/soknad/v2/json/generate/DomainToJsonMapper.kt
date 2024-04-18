package no.nav.sosialhjelp.soknad.v2.json.generate

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import java.util.UUID

interface DomainToJsonMapper {
    fun mapToSoknad(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    )
}
