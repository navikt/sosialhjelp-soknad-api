package no.nav.sosialhjelp.soknad.v2.json.compare

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import java.util.Collections

class MemoryAppender : ListAppender<ILoggingEvent>() {
    fun reset() {
        list.clear()
    }

    fun getErrors(): List<ILoggingEvent> {
        return getLoggedEvents()
            .filter { it.level == Level.ERROR }
    }

    fun getLoggedEvents(): List<ILoggingEvent> {
        return Collections.unmodifiableList(this.list)
    }
}
