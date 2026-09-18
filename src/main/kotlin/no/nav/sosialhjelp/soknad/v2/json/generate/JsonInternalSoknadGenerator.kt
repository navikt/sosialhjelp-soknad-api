package no.nav.sosialhjelp.soknad.v2.json.generate

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataServiceImpl
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

interface DomainToJsonMapper {
    fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad

    fun mapToKortJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad = mapToJson(soknadId, jsonInternalSoknad)
}

@Component
class JsonInternalSoknadGenerator(
    private val mappers: List<DomainToJsonMapper>,
    private val metadataService: SoknadMetadataServiceImpl,
) {
    fun createJsonInternalSoknad(soknadId: UUID): JsonInternalSoknad {
        return mappers
            .fold(JsonInternalSoknad()) { json, mapper ->
                if (metadataService.isKort(soknadId)) mapper.mapToKortJson(soknadId, json) else mapper.mapToJson(soknadId, json)
            }.also { JsonSosialhjelpValidator.ensureValidInternalSoknad(toJson(it)) }
    }

    private companion object {
        private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()

        private fun toJson(jsonInternalSoknad: JsonInternalSoknad) = objectMapper.writeValueAsString(jsonInternalSoknad)
    }
}

private fun SoknadMetadataServiceImpl.isKort(soknadId: UUID) = getSoknadType(soknadId) == SoknadType.KORT

object TimestampUtil {
    private const val ZONE_STRING = "Europe/Oslo"
    private const val TIMESTAMP_REGEX = "^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9].[0-9][0-9]*Z$"
    private const val MILLISECOND = 1000000L

    fun nowWithMillis(): LocalDateTime =
        ZonedDateTime.now(ZoneId.of(ZONE_STRING)).toLocalDateTime().truncatedTo(ChronoUnit.MILLIS)

    fun convertToOffsettDateTimeUTCString(localDateTime: LocalDateTime) = localDateTime.toUTCTimestampStringWithMillis()

    fun parseFromUTCString(utcString: String): LocalDateTime =
        OffsetDateTime
            .parse(utcString)
            .atZoneSameInstant(ZoneId.of(ZONE_STRING))
            .toLocalDateTime()

    fun convertInstantToLocalDateTime(instant: Instant): LocalDateTime =
        LocalDateTime.ofInstant(instant, ZoneId.of(ZONE_STRING))

    private fun validateTimestamp(timestampString: String) {
        if (!Regex(TIMESTAMP_REGEX).matches(timestampString)) error("Tidspunkt $timestampString matcher ikke formatet")
    }

    // I Json-strukturen skal tidspunkt være UTC med 3 desimaler
    private fun LocalDateTime.toUTCTimestampStringWithMillis(): String =
        this
            .let { if (it.nano < MILLISECOND) it.plusNanos(MILLISECOND) else it }
            .atZone(ZoneId.of(ZONE_STRING))
            .withZoneSameInstant(ZoneOffset.UTC)
            .toOffsetDateTime()
            .truncatedTo(ChronoUnit.MILLIS)
            .toString()
            .also { validateTimestamp(it) }
}
