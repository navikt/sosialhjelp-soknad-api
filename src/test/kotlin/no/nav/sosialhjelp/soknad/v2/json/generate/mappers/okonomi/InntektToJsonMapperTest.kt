package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sosialhjelp.soknad.v2.createInntekter
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.InntektToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.getJsonVerdier
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.shadow.okonomi.SoknadJsonTypeEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InntektToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Inntekt med type BARNEBIDRAG_MOTTAR skal mappes til JsonOkonomioversiktInntekt`() {
        val inntekter = createInntekter()

        InntektToJsonMapper(inntekter, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(inntekt).hasSize(2).allMatch { it.type == inntekter.first().type.getJsonVerdier().navn?.verdi }
        }
    }

    @Test
    fun `Inntekt med type NAVYTELSE skal mappes til JsonOkonomiopplysningUtbetaling`() {
        val inntekter =
            setOf(
                Inntekt(
                    type = InntektType.UTBETALING_NAVYTELSE,
                    beskrivelse = "Tittel på Navytelse",
                    inntektDetaljer = OkonomiDetaljer(listOf(createFullNavYtelseInntekt())),
                ),
            )
        InntektToJsonMapper(inntekter, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(utbetaling).hasSize(1)
                .anyMatch { it.type == inntekter.first().type.getJsonVerdier().navn?.verdi }
                .anyMatch { it.komponenter.size == 2 }
        }
    }

    @Test
    fun `Flere okonomiske detaljer skal generere flere Inntekter`() {
        val inntekter =
            setOf(
                Inntekt(
                    type = InntektType.UTBETALING_SALG,
                    beskrivelse = null,
                    inntektDetaljer =
                        OkonomiDetaljer(
                            listOf(
                                Utbetaling(brutto = 4325.0, utbetalingsdato = LocalDate.now().minusDays(5)),
                                Utbetaling(netto = 2435.0, utbetalingsdato = LocalDate.now().minusDays(10)),
                            ),
                        ),
                ),
            )
        InntektToJsonMapper(inntekter, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(utbetaling).hasSize(2).allMatch { it.type == SoknadJsonTypeEnum.UTBETALING_SALG.verdi }
        }
    }

    @Test
    fun `Type med Beskrivelse skal mappes til JsonBeskrivelseAvAnnet`() {
        val inntekter = setOf(Inntekt(InntektType.UTBETALING_ANNET, "Beskrivelse av annet"))

        InntektToJsonMapper(inntekter, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(utbetaling).hasSize(1).allMatch { it.type == SoknadJsonTypeEnum.UTBETALING_ANNET.verdi }
            assertThat(beskrivelseAvAnnet.utbetaling).isEqualTo(inntekter.first().beskrivelse)
        }
    }

    @Test
    fun `Midlertidig mapping til gammel type`() {
        val nyUtgift = Inntekt(type = InntektType.UTBETALING_HUSBANKEN)
        val annenUtgift = Inntekt(type = InntektType.JOBB)

        InntektToJsonMapper(inntekter = setOf(nyUtgift, annenUtgift), jsonOkonomi).doMapping()

        with(jsonOkonomi) {
            assertThat(opplysninger.utbetaling).hasSize(1)
            assertThat(opplysninger.utbetaling.first().type).isEqualTo(SoknadJsonTypeEnum.UTBETALING_HUSBANKEN.verdi)

            assertThat(oversikt.inntekt).hasSize(1)
            assertThat(oversikt.inntekt.first().type).isEqualTo(SoknadJsonTypeEnum.JOBB.verdi)
        }
    }
}

private fun createFullNavYtelseInntekt(): UtbetalingMedKomponent {
    return UtbetalingMedKomponent(
        utbetaling =
            Utbetaling(
                brutto = 1234.00,
                netto = 800.0,
                belop = 400.0,
                skattetrekk = 21.5,
                andreTrekk = 1.5,
                utbetalingsdato = LocalDate.now().minusDays(10),
                periodeFom = LocalDate.now().minusMonths(1),
                periodeTom = null,
                mottaker = Mottaker.HUSSTAND,
            ),
        komponenter =
            listOf(
                Komponent(
                    type = "utbetaling 1",
                    belop = 2400.0,
                    satsType = "lønn",
                    satsAntall = 1.0,
                    satsBelop = 233.0,
                ),
                Komponent(
                    type = "utbetaling 2",
                    belop = 3400.0,
                    satsType = "timelønn",
                    satsAntall = 8.0,
                    satsBelop = 190.0,
                ),
            ),
    )
}
