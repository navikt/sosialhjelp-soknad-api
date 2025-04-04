package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.v2.createInntekter
import no.nav.sosialhjelp.soknad.v2.json.SoknadJsonTypeEnum
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.InntektToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.getSoknadJsonTypeString
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InntektToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Inntekt med type BARNEBIDRAG_MOTTAR skal mappes til JsonOkonomioversiktInntekt`() {
        val inntekter = createInntekter()

        InntektToJsonMapper(inntekter, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(inntekt).hasSize(2).allMatch { it.type == inntekter.first().type.getSoknadJsonTypeString() }
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
                .anyMatch { it.type == inntekter.first().type.getSoknadJsonTypeString() }
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

    @Test
    fun `Inntekt med belop skal mappes til opplysning`() {
        val inntekt =
            Inntekt(
                type = InntektType.UTBETALING_UTBYTTE,
                inntektDetaljer =
                    OkonomiDetaljer(
                        detaljer = listOf(Belop(belop = 2452.0), Belop(belop = 1422.0)),
                    ),
            )

        InntektToJsonMapper(inntekter = setOf(inntekt), jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(utbetaling).hasSize(2)
                .allMatch {
                    it.type == InntektType.UTBETALING_UTBYTTE.getSoknadJsonTypeString() &&
                        it.belop != null
                }
        }
    }

    @Test
    fun `Utbetaling fra NAV skal ha kilde = system`() {
        val brutto = 1000.0
        val netto = 500.0

        val inntekt =
            Inntekt(
                type = InntektType.UTBETALING_NAVYTELSE,
                inntektDetaljer =
                    OkonomiDetaljer(
                        detaljer =
                            listOf(
                                UtbetalingMedKomponent(
                                    tittel = "Barnetrygd",
                                    utbetaling =
                                        Utbetaling(
                                            brutto = brutto,
                                            netto = netto,
                                            utbetalingsdato = LocalDate.now(),
                                            organisasjon =
                                                Organisasjon(
                                                    navn = "NAV Gokkivold",
                                                    orgnummer = "123456789",
                                                ),
                                        ),
                                    komponenter =
                                        listOf(
                                            Komponent(
                                                type = "Ytelseskomponenttype",
                                                belop = 0.0,
                                                satsType = "satstype",
                                                satsAntall = 2.0,
                                                satsBelop = 0.0,
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        InntektToJsonMapper(inntekter = setOf(inntekt), jsonOkonomi).doMapping()

        assertThat(jsonOkonomi.opplysninger.utbetaling)
            .hasSize(1)
            .anyMatch { it.kilde == JsonKilde.SYSTEM }
            .anyMatch { it.tittel == "Barnetrygd" }
            .anyMatch { it.netto == netto && it.belop == netto.toInt() }
    }

    @Test
    fun `Bostotte hentet fra register skal ha OkonomiDetalj Utbetaling og havne i feltet netto`() {
        val inntekt =
            Inntekt(
                type = InntektType.UTBETALING_HUSBANKEN,
                inntektDetaljer =
                    OkonomiDetaljer(
                        detaljer =
                            listOf(
                                Utbetaling(
                                    netto = 1234.0,
                                    utbetalingsdato = LocalDate.now().minusDays(10),
                                    mottaker = Mottaker.HUSSTAND,
                                ),
                            ),
                    ),
            )

        InntektToJsonMapper(inntekter = setOf(inntekt), jsonOkonomi).doMapping()

        assertThat(jsonOkonomi.opplysninger.utbetaling).hasSize(1)
        jsonOkonomi.opplysninger.utbetaling.first().let {
            assertThat(it.kilde).isEqualTo(JsonKilde.SYSTEM)
            assertThat(it.netto).isNotNull()
            assertThat(it.belop == null).isTrue()
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
        tittel = "Barnetrygd",
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
