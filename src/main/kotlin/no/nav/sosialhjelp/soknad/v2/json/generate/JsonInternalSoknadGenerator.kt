package no.nav.sosialhjelp.soknad.v2.json.generate

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
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
        return JsonInternalSoknad()
            .withSoknad(JsonSoknad())
            .withVedlegg(JsonVedleggSpesifikasjon())
            .withMottaker(JsonSoknadsmottaker())
            .withMidlertidigAdresse(JsonAdresse())
            .apply { mappers.forEach { it.mapToSoknad(soknadId, this) } }
            .also { JsonSosialhjelpValidator.ensureValidInternalSoknad(toJson(it)) }
    }

    fun copyAndMerge(soknadId: String, original: JsonInternalSoknad): JsonInternalSoknad {
        return copyJsonInternalSoknad(original)
            .apply {
                mappers.forEach { it.mapToSoknad(UUID.fromString(soknadId), this) }
            }
            .also { JsonSosialhjelpValidator.ensureValidInternalSoknad(toJson(it)) }
    }

    private fun copyJsonInternalSoknad(jsonSoknad: JsonInternalSoknad) = toObject(toJson(jsonSoknad))

    private companion object {
        private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        private fun toJson(jsonInternalSoknad: JsonInternalSoknad) = objectMapper.writeValueAsString(jsonInternalSoknad)
        private fun toObject(json: String) = objectMapper.readValue(json, JsonInternalSoknad::class.java)
    }
}
