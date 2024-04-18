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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class NavUtbetalingerServiceTest {
    private val navUtbetalingerClient: NavUtbetalingerClient = mockk()
    private val navUtbetalingerService = NavUtbetalingerService(navUtbetalingerClient)

    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setup() {
        mapper =
            jacksonObjectMapper()
                .registerKotlinModule()
                .registerModule(JavaTimeModule())
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    }

    @Test
    internal fun clientReturnererUtbetalinger() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns
            UtbetalDataDto(
                listOf(lagUtbetalingResponse()),
                false,
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
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns
            UtbetalDataDto(
                listOf(lagUtbetalingUtenKomponenterResponse()),
                false,
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

    private fun lagUtbetalingResponse(): Utbetaling {
        val utbetaltDato = LocalDate.now().minusDays(2)

        val utbetalingJsonString = """
            {
              "posteringsdato": "$utbetaltDato",
              "utbetaltTil": {
                "aktoertype": "PERSON",
                "ident": "string",
                "navn": "string"
              },
              "utbetalingNettobeloep": 999.5,
              "utbetalingsmelding": "string",
              "utbetalingsdato": "$utbetaltDato",
              "forfallsdato": "2022-10-17",
              "utbetaltTilKonto": {
                "kontonummer": "string",
                "kontotype": "string"
              },
              "utbetalingsmetode": "string",
              "utbetalingsstatus": "string",
              "ytelseListe": [
                {
                  "ytelsestype": "string",
                  "ytelsesperiode": {
                    "fom": "2022-10-17",
                    "tom": "2022-10-17"
                  },
                  "ytelseskomponentListe": [
                    {
                      "ytelseskomponenttype": "string",
                      "satsbeloep": 999,
                      "satstype": "string",
                      "satsantall": 2.5,
                      "ytelseskomponentbeloep": 42
                    }
                  ],
                  "ytelseskomponentersum": 111.22,
                  "trekkListe": [
                    {
                      "trekktype": "string",
                      "trekkbeloep": 100,
                      "kreditor": "string"
                    }
                  ],
                  "trekksum": 1000,
                  "skattListe": [
                    {
                      "skattebeloep": 99.9
                    }
                  ],
                  "skattsum": 1000.5,
                  "ytelseNettobeloep": 1999,
                  "bilagsnummer": "string",
                  "rettighetshaver": {
                    "aktoertype": "PERSON",
                    "ident": "string",
                    "navn": "string"
                  },
                  "refundertForOrg": {
                    "aktoertype": "PERSON",
                    "ident": "string",
                    "navn": "string"
                  }
                }
              ]
            }  
        """

        return mapper.readValue(utbetalingJsonString)
    }

    private fun lagUtbetalingUtenKomponenterResponse(): Utbetaling {
        val utbetaltDato = LocalDate.now().minusDays(2)

        val utbetalingJsonString = """
            {
              "posteringsdato": "$utbetaltDato",
              "utbetaltTil": {
                "aktoertype": "PERSON",
                "ident": "string",
                "navn": "string"
              },
              "utbetalingNettobeloep": 999.5,
              "utbetalingsmelding": "string",
              "utbetalingsdato": "$utbetaltDato",
              "forfallsdato": "2022-10-17",
              "utbetaltTilKonto": {
                "kontonummer": "string",
                "kontotype": "string"
              },
              "utbetalingsmetode": "string",
              "utbetalingsstatus": "string",
              "ytelseListe": [
                {
                  "ytelsestype": "string",
                  "ytelsesperiode": {
                    "fom": "2022-10-17",
                    "tom": "2022-10-17"
                  },
                  "ytelseskomponentersum": 111.22,
                  "trekkListe": [
                    {
                      "trekktype": "string",
                      "trekkbeloep": 100,
                      "kreditor": "string"
                    }
                  ],
                  "trekksum": 1000,
                  "skattListe": [
                    {
                      "skattebeloep": 99.9
                    }
                  ],
                  "skattsum": 1000.5,
                  "ytelseNettobeloep": 1999,
                  "bilagsnummer": "string",
                  "rettighetshaver": {
                    "aktoertype": "PERSON",
                    "ident": "string",
                    "navn": "string"
                  },
                  "refundertForOrg": {
                    "aktoertype": "PERSON",
                    "ident": "string",
                    "navn": "string"
                  }
                }
              ]
            }  
        """

        return mapper.readValue(utbetalingJsonString)
    }
}
