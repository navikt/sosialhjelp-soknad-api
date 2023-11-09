package no.nav.sosialhjelp.soknad.nymodell.fullfort

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.SoknadMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class JsonInternalSoknadCreator (
    private val soknadMapper: SoknadMapper
) {
    fun createNewJsonInternalSoknad(soknadId: UUID) {
        val jsonInternalSoknad = JsonInternalSoknad()
        jsonInternalSoknad.createChildrenIfNotExists()
        mapToExistingJsonInternalSoknad(soknadId, jsonInternalSoknad)
    }

    fun mapToExistingJsonInternalSoknad(soknadId: UUID, json: JsonInternalSoknad): JsonInternalSoknad {
        soknadMapper.mapSoknadToJson(soknadId, json)
        return json
    }
}

fun JsonInternalSoknad.createChildrenIfNotExists() {
    if (soknad == null) withSoknad(JsonSoknad())
    if (soknad.data == null) soknad.withData(JsonData())
}
