package no.nav.sosialhjelp.soknad.v2.json.generate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
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

        // TODO Json-validering (finnes i filformat-biblioteket)

        return jsonSoknad
    }

    fun copyAndMerge(soknadId: String, original: JsonInternalSoknad): JsonInternalSoknad {
        return copyJsonInternalSoknad(original).also { copy ->
            mappers.forEach {
                it.mapToSoknad(
                    UUID.fromString(soknadId),
                    copy
                )
            }
        }
    }

    private fun copyJsonInternalSoknad(jsonSoknad: JsonInternalSoknad): JsonInternalSoknad {
        return JsonSosialhjelpObjectMapper.createObjectMapper()
            .let {
                val jsonString = it.writeValueAsString(jsonSoknad)
                it.readValue(jsonString, JsonInternalSoknad::class.java)
            }
    }
}
