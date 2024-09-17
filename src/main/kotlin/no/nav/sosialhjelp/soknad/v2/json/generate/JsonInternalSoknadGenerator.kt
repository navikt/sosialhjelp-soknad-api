package no.nav.sosialhjelp.soknad.v2.json.generate

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

interface DomainToJsonMapper {
    fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    )
}

@Component
class JsonInternalSoknadGenerator(
    private val mappers: List<DomainToJsonMapper>,
) {
    private val logger: Logger = LoggerFactory.getLogger(JsonInternalSoknadGenerator::class.java)

    fun createJsonInternalSoknad(soknadId: UUID): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withSoknad(JsonSoknad())
            .withVedlegg(JsonVedleggSpesifikasjon())
            .withMottaker(JsonSoknadsmottaker())
            .withMidlertidigAdresse(JsonAdresse())
            .apply { mappers.forEach { it.mapToJson(soknadId, this) } }
            .also { JsonSosialhjelpValidator.ensureValidInternalSoknad(toJson(it)) }
    }

    fun copyAndMerge(
        soknadId: String,
        original: JsonInternalSoknad,
    ): JsonInternalSoknad {
        return copyJsonInternalSoknad(original)
            .apply {
                mappers.forEach { it.mapToJson(UUID.fromString(soknadId), this) }
            }
            .also {
                runCatching {
                    JsonSosialhjelpValidator.ensureValidInternalSoknad(toJson(it))
                }
                    .onFailure {
                        logger.warn("Feil i sammenlikning av json", it)
                    }
            }
    }

    private fun copyJsonInternalSoknad(jsonSoknad: JsonInternalSoknad) = toObject(toJson(jsonSoknad))

    private companion object {
        private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

        private fun toJson(jsonInternalSoknad: JsonInternalSoknad) = objectMapper.writeValueAsString(jsonInternalSoknad)

        private fun toObject(json: String) = objectMapper.readValue(json, JsonInternalSoknad::class.java)
    }
}

object TimestampManager {
    private const val ZONE_STRING = "Europe/Oslo"
    private const val TIMESTAMP_REGEX = "^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9].[0-9][0-9]*Z$"
    private const val MILLISECOND = 1000000L

    fun convertToOffsettDateTimeUTCString(localDateTime: LocalDateTime) = localDateTime.toUTCTimestampStringWithMillis()

    private fun validateTimestamp(timestampString: String) {
        if (!Regex(TIMESTAMP_REGEX).matches(timestampString)) error("Tidspunkt $timestampString matcher ikke formatet")
    }

    // I Json-strukturen skal tidspunkt være UTC med 3 desimaler
    private fun LocalDateTime.toUTCTimestampStringWithMillis(): String {
        return this
            .let { if (it.nano < MILLISECOND) it.plusNanos(MILLISECOND) else it }
            .atZone(ZoneId.of(ZONE_STRING))
            .withZoneSameInstant(ZoneOffset.UTC)
            .toOffsetDateTime()
            .truncatedTo(ChronoUnit.MILLIS)
            .toString()
            .also { validateTimestamp(it) }
    }
}
