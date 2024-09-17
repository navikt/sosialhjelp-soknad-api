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
import org.skyscreamer.jsonassert.FieldComparisonFailure
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class JsonContentComparatorTest {
    private val comparator = ShadowProductionManager.JsonContentComparator()
    private var original: JsonInternalSoknad = createJsonInternalSoknad()
    private val logger = LoggerFactory.getLogger(JsonCompareErrorLogger::class.java)

    private val logInMemoryAppender =
        object : ListAppender<ILoggingEvent>() {
            fun getErrors(): List<ILoggingEvent> = list.filter { it.level == Level.WARN }
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

        val other =
            copyJsonClass(original).apply {
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
                    },
                ).isFalse()
                assertThat(any { it.contains("barn.personIdentifikator") }).isTrue()
                assertThat(any { it.contains("ektefelle.personIdentifikator") }).isTrue()
                assertThat(any { it.contains("personalia.personIdentifikator.verdi") }).isTrue()
            }
    }

    @Test
    fun `Adresseinformasjon skal ikke finnes i logg etter sammenlikning`() {
        val originalAdresse = createGateAdresse()
        val other =
            copyJsonClass(originalAdresse).apply {
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
        val other =
            copyJsonClass(originalPersonalia).apply {
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

    @Test
    fun `Felt som ikke skal merkes som feil`() {
        val tittel1 = "soknad.data.okonomi.opplysninger.utgift[543534].tittel"
        val tittel2 = "soknad.data.okonomi.opplysninger.utgift[0].tittel"
        val type1 = "soknad.data.okonomi.opplysninger.utgift[31].type"
        val type2 = "soknad.data.okonomi.opplysninger.utgift[0].type"

        ExpectedDiffHandler.isExpectedDiff(tittel1).also { assertThat(it).isTrue() }
        ExpectedDiffHandler.isExpectedDiff(tittel2).also { assertThat(it).isTrue() }
        ExpectedDiffHandler.isExpectedDiff(type1).also { assertThat(it).isTrue() }
        ExpectedDiffHandler.isExpectedDiff(type2).also { assertThat(it).isTrue() }
    }

    @Test
    fun `Felt som skal skrives ut med verdie (for sammenlikning)`() {
        val now = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(3).truncatedTo(ChronoUnit.MILLIS)
        val now2 = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS)

        FieldComparisonFailure("soknad.innsendingstidspunkt", now.toString(), now2.toString())
            .let { ErrorStringHandler.createErrorString(it) }
            .also {
                assertThat(it).contains(now.toString())
                assertThat(it).contains(now2.toString())
            }

        FieldComparisonFailure("noe.annet", "annet1", "annet2")
            .let { ErrorStringHandler.createErrorString(it) }
            .also {
                assertThat(it).doesNotContain("annet1")
                assertThat(it).doesNotContain("annet2")
            }
    }
}
