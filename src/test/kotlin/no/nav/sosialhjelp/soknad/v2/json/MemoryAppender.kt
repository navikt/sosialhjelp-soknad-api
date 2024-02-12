package no.nav.sosialhjelp.soknad.v2.json

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import java.util.*
import java.util.stream.Collectors


class MemoryAppender : ListAppender<ILoggingEvent>() {

    fun reset() {
        list.clear()
    }

    fun contains(string: String?, level: Level): Boolean {
        return list.stream()
            .anyMatch { event: ILoggingEvent ->
                event.toString().contains(string!!) && event.level == level
            }
    }

    fun countEventsForLogger(loggerName: String?): Int {
        return list.stream()
            .filter { event: ILoggingEvent ->
                event.loggerName.contains(loggerName!!)
            }
            .count().toInt()
    }

    fun search(string: String?): List<ILoggingEvent> {
        return list.stream()
            .filter { event: ILoggingEvent ->
                event.toString().contains(string!!)
            }
            .collect(Collectors.toList())
    }

    fun search(string: String?, level: Level): List<ILoggingEvent> {
        return list.stream()
            .filter { event: ILoggingEvent ->
                event.toString().contains(string!!) && event.level == level
            }
            .collect(Collectors.toList())
    }

    fun getErrors(): List<ILoggingEvent> {
        return getLoggedEvents()
            .filter { it.level == Level.ERROR }
    }

    fun getSize(): Int {
        return list.size
    }

    fun getLoggedEvents(): List<ILoggingEvent> {
        return Collections.unmodifiableList(this.list)
    }
}
