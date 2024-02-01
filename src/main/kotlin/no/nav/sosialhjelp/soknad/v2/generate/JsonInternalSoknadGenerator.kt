package no.nav.sosialhjelp.soknad.v2.generate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import org.springframework.stereotype.Component
import java.util.*

@Component
class JsonInternalSoknadGenerator(
    private val mappers: List<DomainToJsonMapper>
) {
    fun createJsonInternalSoknad(soknadId: UUID): JsonInternalSoknad {
        val jsonSoknad = JsonInternalSoknad()
            .withSoknad(JsonSoknad())
            .withVedlegg(JsonVedleggSpesifikasjon())
            .withMottaker(JsonSoknadsmottaker())
            .withMidlertidigAdresse(JsonAdresse())

        mappers.forEach { it.mapToSoknad(soknadId, jsonSoknad) }

        // TODO Validate ?

        return jsonSoknad
    }

    fun copyAndMerge(soknadId: UUID, orgJson: JsonInternalSoknad): JsonInternalSoknad {
        return copyJsonInternalSoknad(orgJson).also { json ->
            mappers.forEach { it.mapToSoknad(soknadId, json) }
        }
    }

    private fun copyJsonInternalSoknad(jsonSoknad: JsonInternalSoknad): JsonInternalSoknad {
        return jacksonObjectMapper().run {
            val jsonString = writeValueAsString(jsonSoknad)
            readValue(jsonString, JsonInternalSoknad::class.java)
        }
    }
}
