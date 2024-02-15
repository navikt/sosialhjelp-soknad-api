package no.nav.sosialhjelp.soknad.v2.json.compare

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.copyJsonClass
import no.nav.sosialhjelp.soknad.v2.createGateAdresse
import no.nav.sosialhjelp.soknad.v2.createJsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.createJsonPersonalia
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.UUID

class JsonStructureComparatorTest {

    private val comparator = JsonStructureComparator(UUID.randomUUID())
    private var original: JsonInternalSoknad = createJsonInternalSoknad()
    private val logger = LoggerFactory.getLogger(JsonCompareErrorLogger::class.java)
    private lateinit var memoryAppender: MemoryAppender

    @BeforeEach
    fun setup() {
        memoryAppender = MemoryAppender().also {
            it.context = LoggerFactory.getILoggerFactory() as LoggerContext
            (logger as Logger).addAppender(it)
            it.start()
        }
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

        memoryAppender.getErrors()
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

        memoryAppender.getErrors().map { it.message }.forEach { logString ->
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

        memoryAppender.getErrors()
            .map { it.message }
            .filter { it.contains("oppholdsadresse.type") }
            .let {
                assertThat(it).isNotEmpty
                assertThat(it.first().contains("** FieldFailure **")).isTrue()
                assertThat(it.first().contains(originalPersonalia.oppholdsadresse.type.value())).isTrue()
                assertThat(it.first().contains(other.oppholdsadresse.type.value())).isTrue()
            }
    }
}
