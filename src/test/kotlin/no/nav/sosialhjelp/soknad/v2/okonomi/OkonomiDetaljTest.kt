package no.nav.sosialhjelp.soknad.v2.okonomi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OkonomiDetaljTest {
    @Test
    fun `Mappe liste med Belop til json og tilbake skal fungere`() {
        val okonomiskeDetaljer: OkonomiskeDetaljer<Belop> =
            OkonomiskeDetaljer(
                listOf(
                    Belop(belop = 40.0),
                    Belop(belop = 50.0),
                ),
            )

        val jsonString = OkonomiskeDetaljerToStringConverter<Belop>().convert(okonomiskeDetaljer)
        assertThat(jsonString).contains("40.0").contains(("50.0"))

        val okonomidetaljer2 = StringToOkonomiskeDetaljerConverter<Belop>().convert(jsonString)
        assertThat(okonomidetaljer2.detaljer.size).isEqualTo(okonomiskeDetaljer.detaljer.size)
        assertThat(okonomidetaljer2.detaljer).allMatch { it is Belop }
    }

    @Test
    fun `Mappe BruttoNetto til json og tilbake skal fungere`() {
        val okonomiskeDetaljer =
            OkonomiskeDetaljer(
                listOf(
                    BruttoNetto(brutto = 50.0, netto = 40.0),
                    BruttoNetto(brutto = 70.0, netto = 30.0),
                ),
            )

        val json = OkonomiskeDetaljerToStringConverter<BruttoNetto>().convert(okonomiskeDetaljer)
        assertThat(json).contains("50.0").contains("40.0").contains("70.0").contains("30.0")

        val okonomidetaljer2 = StringToOkonomiskeDetaljerConverter<BruttoNetto>().convert(json)
        assertThat(okonomidetaljer2.detaljer.size).isEqualTo(okonomiskeDetaljer.detaljer.size)
        assertThat(okonomidetaljer2.detaljer).allMatch { it is BruttoNetto }
    }

    @Test
    fun `Mappe Utbetaling til json og tilbake skal fungere`() {
        val now = LocalDate.now()
        val okonomiskeDetaljer =
            OkonomiskeDetaljer(
                listOf(
                    Utbetaling(brutto = 50.0, netto = 40.0, utbetalingsdato = now),
                    Utbetaling(brutto = 70.0, netto = 30.0, periodeTom = now),
                ),
            )

        val json = OkonomiskeDetaljerToStringConverter<Utbetaling>().convert(okonomiskeDetaljer)
        assertThat(json).contains("50.0").contains("40.0").contains("70.0").contains("30.0")
        assertThat(okonomiskeDetaljer.detaljer).anyMatch { (it as Utbetaling).utbetalingsdato == now }
        assertThat(okonomiskeDetaljer.detaljer).anyMatch { (it as Utbetaling).periodeTom == now }

        val okonomidetaljer2 = StringToOkonomiskeDetaljerConverter<Utbetaling>().convert(json)
        assertThat(okonomidetaljer2.detaljer.size).isEqualTo(okonomiskeDetaljer.detaljer.size)
        assertThat(okonomidetaljer2.detaljer).allMatch { it is Utbetaling }
    }

    @Test
    fun `Mappe UtbetalingMedKomponenter til json og tilbake skal fungere`() {
        val okonomiskeDetaljer =
            OkonomiskeDetaljer(
                listOf(
                    UtbetalingMedKomponent(
                        utbetaling = Utbetaling(brutto = 50.0, netto = 40.0, utbetalingsdato = LocalDate.now()),
                        komponenter =
                            listOf(
                                Komponent(type = "Komponent 1", satsBelop = 40.0),
                                Komponent(type = "Komponent 2", satsBelop = 60.0),
                            ),
                    ),
                    UtbetalingMedKomponent(
                        utbetaling = Utbetaling(brutto = 70.0, netto = 30.0, periodeTom = LocalDate.now()),
                    ),
                ),
            )

        val json = OkonomiskeDetaljerToStringConverter<UtbetalingMedKomponent>().convert(okonomiskeDetaljer)
        assertThat(json).contains("50.0").contains("40.0").contains("70.0").contains("30.0").contains("Komponent 1")

        val okonomidetaljer2 = StringToOkonomiskeDetaljerConverter<UtbetalingMedKomponent>().convert(json)
        assertThat(okonomidetaljer2.detaljer.size).isEqualTo(okonomiskeDetaljer.detaljer.size)
        assertThat(okonomidetaljer2.detaljer).allMatch { it is UtbetalingMedKomponent }
        assertThat(okonomidetaljer2.detaljer).anyMatch { (it as UtbetalingMedKomponent).komponenter.size == 2 }
    }
}
