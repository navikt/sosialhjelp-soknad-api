package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenClient
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.SkattbarInntektDto
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.UUID

class InntektSkatteetatenIntegrationTest : AbstractOkonomiIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @MockkBean
    private lateinit var skatteetatenClient: SkatteetatenClient

    @BeforeEach
    fun mockAnswer() {
        every { skatteetatenClient.hentSkattbarinntekt(any()) } returns readResponseFromPath()
    }

    @Test
    fun `Inntekt fra flere Firma skal returnere riktige data`() {
        val inntekt = opprettDataForInntektSkatt()

        val dto =
            doGet(
                uri = skattUrl(soknad.id),
                responseBodyClass = SkattbarInntektDto::class.java,
            )

        assertThat(dto.samtykke?.verdi).isTrue()
        assertThat(dto.inntektFraSkatteetatenFeilet).isFalse()

        assertThat(dto.inntektSkatteetaten).hasSize(inntekt.inntektDetaljer.detaljer.size)

        inntekt.inntektDetaljer.detaljer
            .map { it as Utbetaling }
            .forEach { domainUtbetaling ->

                dto.inntektSkatteetaten
                    .flatMap { it.organisasjoner }
                    .also { orgDtos ->
                        assertThat(orgDtos)
                            .anyMatch { domainUtbetaling.organisasjon?.navn == it.organisasjonsnavn }
                            .anyMatch { domainUtbetaling.organisasjon?.orgnummer == it.orgnr }
                    }
                    .flatMap { it.utbetalinger }
                    .also { dtoUtbetalinger ->
                        assertThat(dtoUtbetalinger)
                            .anyMatch { domainUtbetaling.brutto == it.brutto }
                            .anyMatch { domainUtbetaling.skattetrekk == it.forskuddstrekk }
                            .anyMatch { domainUtbetaling.tittel == it.tittel }
                    }
            }
    }

    @Test
    fun `Inntekt for samme periode skal returnere riktige data`() {
        val inntekt = opprettDataForInntektSkatt(utbetalinger = utbetalingerSammePeriode())

        val dto =
            doGet(
                uri = skattUrl(soknad.id),
                responseBodyClass = SkattbarInntektDto::class.java,
            )

        assertThat(dto.samtykke?.verdi).isTrue()
        assertThat(dto.inntektFraSkatteetatenFeilet).isFalse()

        assertThat(dto.inntektSkatteetaten).hasSize(1)
        assertThat(dto.inntektSkatteetaten.first().organisasjoner).hasSize(2)

        inntekt.inntektDetaljer.detaljer
            .map { it as Utbetaling }
            .forEach { domainUtbetaling ->

                dto.inntektSkatteetaten
                    .flatMap { it.organisasjoner }
                    .also { orgDtos ->
                        assertThat(orgDtos)
                            .anyMatch { domainUtbetaling.organisasjon?.navn == it.organisasjonsnavn }
                            .anyMatch { domainUtbetaling.organisasjon?.orgnummer == it.orgnr }
                    }
                    .flatMap { it.utbetalinger }
                    .also { dtoUtbetalinger ->
                        assertThat(dtoUtbetalinger)
                            .anyMatch { domainUtbetaling.brutto == it.brutto }
                            .anyMatch { domainUtbetaling.skattetrekk == it.forskuddstrekk }
                            .anyMatch { domainUtbetaling.tittel == it.tittel }
                    }
            }
    }

    @Test
    fun `Sette samtykke til true skal trigge innhenting av data`() {
        okonomiService.getBekreftelser(soknad.id).let { assertThat(it).isEmpty() }
        okonomiService.getInntekter(soknad.id).let { assertThat(it).isEmpty() }

        doPut(
            uri = updateUrl(soknad.id),
            requestBody = true,
            responseBodyClass = SkattbarInntektDto::class.java,
            soknadId = soknad.id,
        ).also {
            assertThat(it.samtykke?.verdi).isTrue()
            assertThat(it.inntektSkatteetaten).hasSize(2)
        }

        verify(exactly = 1) { skatteetatenClient.hentSkattbarinntekt(any()) }

        okonomiService.getBekreftelser(soknad.id).let { bekreftelser ->
            assertThat(bekreftelser.toList())
                .hasSize(1)
                .allMatch { it.type == BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE }
        }

        okonomiService.getInntekter(soknad.id).let { inntekter ->
            assertThat(inntekter.toList()).hasSize(1).allMatch { it.type == InntektType.UTBETALING_SKATTEETATEN }
            assertThat(inntekter.first().inntektDetaljer.detaljer).hasSize(2)
        }
    }

    @Test
    fun `Endre samtykke til false skal slette data`() {
        opprettDataForInntektSkatt()

        okonomiService.getBekreftelser(soknad.id).let { assertThat(it).hasSize(1) }
        okonomiService.getInntekter(soknad.id).let { assertThat(it).hasSize(1) }

        doPut(
            uri = updateUrl(soknad.id),
            requestBody = false,
            responseBodyClass = SkattbarInntektDto::class.java,
            soknadId = soknad.id,
        ).also {
            assertThat(it.samtykke?.verdi).isFalse()
            assertThat(it.inntektFraSkatteetatenFeilet).isFalse()
            assertThat(it.inntektSkatteetaten).isEmpty()
        }

        assertThat(okonomiService.getBekreftelser(soknad.id).toList())
            .hasSize(1).allMatch { it.type == BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE }

        assertThat(okonomiService.getInntekter(soknad.id).toList())
            .hasSize(1).allMatch { it.type == InntektType.JOBB }
    }

    @Test
    fun `Oppdatere allerede satt samtykke skal ikke trigge ny innhenting`() {
        opprettDataForInntektSkatt()

        doPut(
            uri = updateUrl(soknad.id),
            requestBody = true,
            responseBodyClass = SkattbarInntektDto::class.java,
            soknadId = soknad.id,
        ).also {
            assertThat(it.samtykke?.verdi).isTrue()
        }

        verify(exactly = 0) { skatteetatenClient.hentSkattbarinntekt(any()) }
    }

    private fun opprettDataForInntektSkatt(utbetalinger: List<Utbetaling> = utbetalingerForskjelligePeriode()): Inntekt {
        integrasjonStatusService.setInntektSkatteetatenStatus(soknad.id, false)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE, true)
        okonomiService.addElementToOkonomi(
            soknadId = soknad.id,
            element =
                Inntekt(
                    type = InntektType.UTBETALING_SKATTEETATEN,
                    inntektDetaljer = OkonomiDetaljer(utbetalinger),
                ),
        )
        return okonomiService.getInntekter(soknad.id).first()
    }

    private fun utbetalingerSammePeriode(): List<Utbetaling> {
        return listOf(
            Utbetaling(
                brutto = 2500.0,
                skattetrekk = 500.0,
                tittel = "Lønn",
                periodeFom = LocalDate.now().minusMonths(1),
                organisasjon =
                    Organisasjon(
                        navn = "Firma AS",
                        orgnummer = "123456789",
                    ),
            ),
            Utbetaling(
                brutto = 5000.0,
                skattetrekk = 1000.0,
                tittel = "Frynsegoder",
                periodeFom = LocalDate.now().minusMonths(1),
                periodeTom = LocalDate.now(),
                organisasjon =
                    Organisasjon(
                        navn = "Korrupsjon AS",
                        orgnummer = "987654321",
                    ),
            ),
        )
    }

    private fun utbetalingerForskjelligePeriode(): List<Utbetaling> {
        return listOf(
            Utbetaling(
                brutto = 2500.0,
                skattetrekk = 500.0,
                tittel = "Lønn",
                periodeFom = LocalDate.now().minusMonths(1),
                organisasjon =
                    Organisasjon(
                        navn = "Firma AS",
                        orgnummer = "123456789",
                    ),
            ),
            Utbetaling(
                brutto = 5000.0,
                skattetrekk = 1000.0,
                tittel = "Frynsegoder",
                periodeFom = LocalDate.now().minusMonths(2),
                periodeTom = LocalDate.now().minusMonths(1),
                organisasjon =
                    Organisasjon(
                        navn = "Korrupsjon AS",
                        orgnummer = "987654321",
                    ),
            ),
        )
    }

    companion object {
        private fun skattUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/skattbarinntekt"

        private fun updateUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/skattbarinntekt/samtykke"
    }

    private fun readResponseFromPath(path: String = "/skatt/InntektOgSkattToMaanederToArbeidsgivere.json"): SkattbarInntekt {
        val resourceAsStream = this.javaClass.getResourceAsStream(path) ?: error("Resource not found: $path")
        val json = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
        return jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(json, SkattbarInntekt::class.java)
    }
}
