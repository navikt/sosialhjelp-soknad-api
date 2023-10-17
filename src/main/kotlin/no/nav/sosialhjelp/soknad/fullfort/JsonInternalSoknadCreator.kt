package no.nav.sosialhjelp.soknad.fullfort

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import org.springframework.stereotype.Component
import java.util.*

interface SoknadToJsonMapper {
    fun mapToSoknadJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad)
}

@Component
class JsonInternalSoknadCreator (
    private val mappers: List<SoknadToJsonMapper>,
) {
    fun createJsonInternalSoknad(soknadId: UUID): JsonInternalSoknad {
        val jsonInternalSoknad = JsonInternalSoknad()
        mapJsonInternalSoknad(soknadId, jsonInternalSoknad)
        return jsonInternalSoknad
    }

    fun mapJsonInternalSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        mappers.forEach { it.mapToSoknadJson(soknadId, jsonInternalSoknad) }
    }
}