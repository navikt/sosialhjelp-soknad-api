package no.nav.sosialhjelp.soknad.v2.json.compare

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.json.copyJsonClass
import no.nav.sosialhjelp.soknad.v2.json.createGateAdresse
import no.nav.sosialhjelp.soknad.v2.json.createJsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.json.createJsonPersonalia
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*

class JsonContentComparatorTest {

    private val comparator = ShadowProductionManager.JsonContentComparator(UUID.randomUUID().toString())
    private var original: JsonInternalSoknad = createJsonInternalSoknad()
    private val logger = LoggerFactory.getLogger(JsonCompareErrorLogger::class.java)

    private val logInMemoryAppender = object : ListAppender<ILoggingEvent>() {
        fun getErrors(): List<ILoggingEvent> = list.filter { it.level == Level.ERROR }
    }

    @BeforeEach
    fun setup() {
        logInMemoryAppender.context = LoggerFactory.getILoggerFactory() as LoggerContext
        (logger as Logger).addAppender(logInMemoryAppender)
        logInMemoryAppender.start()
    }

    @Test
    fun `PersonId skal ikke finnes i log etter sammenlikning`() {
        val nyPersonId = "99887712345"

        val other = copyJsonClass(original).apply {
            soknad.data.personalia.personIdentifikator.verdi = nyPersonId
            soknad.data.familie.sivilstatus.ektefelle.personIdentifikator = nyPersonId
            soknad.data.familie.forsorgerplikt.ansvar.first().barn.personIdentifikator = nyPersonId
        }

        comparator.doCompareAndLogErrors(original, other)

        logInMemoryAppender.getErrors()
            .map { it.message }
            .run {
                assertThat(any { it.contains(nyPersonId) }).isFalse()
                assertThat(
                    any {
                        it.contains(original.soknad.data.personalia.personIdentifikator.verdi)
                    }
                ).isFalse()
                assertThat(any { it.contains("barn.personIdentifikator") }).isTrue()
                assertThat(any { it.contains("ektefelle.personIdentifikator") }).isTrue()
                assertThat(any { it.contains("personalia.personIdentifikator.verdi") }).isTrue()
            }
    }

    @Test
    fun `Adresseinformasjon skal ikke finnes i logg etter sammenlikning`() {

        val originalAdresse = createGateAdresse()
        val other = copyJsonClass(originalAdresse).apply {
            gatenavn = "En helt annen gate"
            husnummer = "44"
            kommunenummer = "0302"
        }

        comparator.doCompareAndLogErrors(originalAdresse, other)

        logInMemoryAppender.getErrors().map { it.message }.forEach { logString ->
            val error = logString.substring(logString.indexOf("**"), logString.length)

            assertThat(error.contains(other.gatenavn)).isFalse()
            assertThat(error.contains(other.husnummer)).isFalse()
            assertThat(error.contains(other.kommunenummer)).isFalse()
            assertThat(error.contains(originalAdresse.gatenavn)).isFalse()
            assertThat(error.contains(originalAdresse.husnummer)).isFalse()
            assertThat(error.contains(originalAdresse.kommunenummer)).isFalse()
        }
    }

    @Test
    fun `Hvis adressetypen er forskjellig, skal verdi vises i logg`() {
        val originalPersonalia = createJsonPersonalia()
        val other = copyJsonClass(originalPersonalia).apply {
            this.oppholdsadresse = createGateAdresse()
        }
        comparator.doCompareAndLogErrors(originalPersonalia, other)

        logInMemoryAppender.getErrors()
            .map { it.message }
            .filter { it.contains("oppholdsadresse.type") }
            .let {
                assertThat(it).isNotEmpty
                assertThat(it.first().contains("** FieldFailure **")).isTrue()
                assertThat(originalPersonalia.oppholdsadresse).isNotEqualTo(other.oppholdsadresse)
            }
    }
}
