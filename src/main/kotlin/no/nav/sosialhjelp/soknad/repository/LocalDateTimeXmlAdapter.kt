package no.nav.sosialhjelp.soknad.repository

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

class LocalDateTimeXmlAdapter : XmlAdapter<String, LocalDateTime>() {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val temporalQuery: TemporalQuery<LocalDateTime> = TemporalQuery { temporal: TemporalAccessor ->
        LocalDateTime.from(temporal)
    }

    override fun unmarshal(stringValue: String): LocalDateTime {
        return formatter.parse(stringValue, temporalQuery)
    }

    override fun marshal(value: LocalDateTime): String {
        return formatter.format(value)
    }
}
