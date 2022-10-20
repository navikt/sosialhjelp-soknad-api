package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.time.LocalDate

internal class NavUtbetalingerServiceTest {

    private val navUtbetalingerClient: NavUtbetalingerClient = mockk()
    private val navUtbetalingerService = NavUtbetalingerService(navUtbetalingerClient)

    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setup() {
        mapper = jacksonObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    }

    @Test
    internal fun clientReturnererUtbetalinger() {
        val utbetaling = getUtbetalingFromJsonFile("inntekt/navutbetalinger/sokos-utbetaltdata-ekstern-response.json")
        utbetaling.utbetalingsdato = LocalDate.now().minusDays(2)
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns UtbetalDataDto(
            listOf(utbetaling),
            false
        )

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).hasSize(1)
        val navUtbetaling = navUtbetalinger!![0]
        assertThat(navUtbetaling.type).isEqualTo("navytelse")
        assertThat(navUtbetaling.netto).isEqualTo(1999.0)
        assertThat(navUtbetaling.brutto).isEqualTo(111.22)
        assertThat(navUtbetaling.skattetrekk).isEqualTo(1000.5)
        assertThat(navUtbetaling.andreTrekk).isEqualTo(1000.0)
        assertThat(navUtbetaling.bilagsnummer).isEqualTo("string")
        assertThat(navUtbetaling.utbetalingsdato).isEqualTo(LocalDate.now().minusDays(2))
        assertThat(navUtbetaling.periodeFom).isEqualTo(LocalDate.of(2022, 10, 17))
        assertThat(navUtbetaling.periodeTom).isEqualTo(LocalDate.of(2022, 10, 17))
        assertThat(navUtbetaling.komponenter).hasSize(1)
        assertThat(navUtbetaling.komponenter[0].type).isEqualTo("string")
        assertThat(navUtbetaling.komponenter[0].belop).isEqualTo(42.0)
        assertThat(navUtbetaling.komponenter[0].satsType).isEqualTo("string")
        assertThat(navUtbetaling.komponenter[0].satsBelop).isEqualTo(999.0)
        assertThat(navUtbetaling.komponenter[0].satsAntall).isEqualTo(2.5)
        assertThat(navUtbetaling.tittel).isEqualTo("string")
        assertThat(navUtbetaling.orgnummer).isEqualTo("889640782")
    }

    @Test
    internal fun clientReturnererUtbetalingerUtenKomponenter() {
        val utbetaling = getUtbetalingFromJsonFile("inntekt/navutbetalinger/sokos-utbetaltdata-ekstern-response-uten-komponenter.json")
        utbetaling.utbetalingsdato = LocalDate.now().minusDays(2)

        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns UtbetalDataDto(listOf(utbetaling), false)

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).hasSize(1)
        val navUtbetaling = navUtbetalinger!![0]
        assertThat(navUtbetaling.type).isEqualTo("navytelse")
        assertThat(navUtbetaling.netto).isEqualTo(1999.0)
        assertThat(navUtbetaling.brutto).isEqualTo(111.22)
        assertThat(navUtbetaling.skattetrekk).isEqualTo(1000.5)
        assertThat(navUtbetaling.andreTrekk).isEqualTo(1000.0)
        assertThat(navUtbetaling.bilagsnummer).isEqualTo("string")
        assertThat(navUtbetaling.utbetalingsdato).isEqualTo(LocalDate.now().minusDays(2))
        assertThat(navUtbetaling.periodeFom).isEqualTo(LocalDate.of(2022, 10, 17))
        assertThat(navUtbetaling.periodeTom).isEqualTo(LocalDate.of(2022, 10, 17))
        assertThat(navUtbetaling.komponenter).hasSize(0)
        assertThat(navUtbetaling.tittel).isEqualTo("string")
        assertThat(navUtbetaling.orgnummer).isEqualTo("889640782")
    }

    @Test
    internal fun clientReturnererTomListe() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns UtbetalDataDto(emptyList(), true)

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).isNull()
    }

    @Test
    internal fun clientReturnererNull() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns null

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).isNull()
    }

    @Test
    internal fun clientReturnererResponseMedFeiletTrue() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns UtbetalDataDto(null, true)

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).isNull()
    }

    private fun getUtbetalingFromJsonFile(file: String): Utbetaling {
        val resourceAsStream =
            ClassLoader.getSystemResourceAsStream(file)
        assertThat(resourceAsStream).isNotNull
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
        return mapper.readValue<Utbetaling>(jsonString)
    }
}
