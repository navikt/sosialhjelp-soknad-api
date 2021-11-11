package no.nav.sosialhjelp.soknad.skattbarinntekt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.skattbarinntekt.dto.SkattbarInntekt
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.charset.StandardCharsets

internal class SkattbarInntektServiceNyTest {

    private val skatteetatenClient: SkatteetatenClient = mockk()
    private val skattbarInntektServiceNy = SkattbarInntektServiceNy(skatteetatenClient)

    @Test
    internal fun `hentSkattbarInntekt`() {
        val skattbarInntekt = readResponseFromPath("/skatt/InntektOgSkatt.json")
        every { skatteetatenClient.hentSkattbarinntekt(any()) } returns skattbarInntekt

        val utbetalinger = skattbarInntektServiceNy.hentUtbetalinger("01234567")
        val utbetalingPerTittel = utbetalinger?.groupBy { it.tittel }
        val lonn = utbetalingPerTittel?.get("Lønnsinntekt")!!

        val utbetaling = lonn[0]
        assertThat(utbetaling.brutto).isPositive
    }

    @Test
    fun `hentSkattbarInntekt for 2 maaneder ignorerer da arbeidsgiver 1 i forrige maaned`() {
        val skattbarInntekt = readResponseFromPath("/skatt/InntektOgSkattToMaaneder.json")
        every { skatteetatenClient.hentSkattbarinntekt(any()) } returns skattbarInntekt
        val utbetalinger = skattbarInntektServiceNy.hentUtbetalinger("01234567")
        assertThat(utbetalinger).hasSize(2)
    }

    @Test
    fun `hentSkattbarInntekt for 2 maaneder i forrige maaned begge maanedene og arbeidsgiverne vil vaere med`() {
        val skattbarInntekt = readResponseFromPath("/skatt/InntektOgSkattToMaanederToArbeidsgivere.json")
        every { skatteetatenClient.hentSkattbarinntekt(any()) } returns skattbarInntekt
        val utbetalinger = skattbarInntektServiceNy.hentUtbetalinger("01234567")
        assertThat(utbetalinger).hasSize(2)
        assertThat(utbetalinger!!.groupBy { it.orgnummer }.entries).hasSize(2)
    }

    private fun readResponseFromPath(path: String): SkattbarInntekt? {
        return try {
            val resourceAsStream = this.javaClass.getResourceAsStream(path) ?: return null
            val json = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
            jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(json, SkattbarInntekt::class.java)
        } catch (e: IOException) {
            null
        }
    }
}
