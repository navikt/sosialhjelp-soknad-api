package no.nav.sosialhjelp.soknad.v2.okonomi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OkonomiRadTest {
    @Test
    fun `Mappe liste med Belop til json og tilbake skal fungere`() {
        val okonomiRader: OkonomiRader<Belop> =
            OkonomiRader(
                listOf(
                    Belop(belop = 40.0),
                    Belop(belop = 50.0),
                ),
            )

        val jsonString = OkonomiRaderToStringConverter<Belop>().convert(okonomiRader)
        assertThat(jsonString).contains("40.0").contains(("50.0"))

        val okonomiRader2 = StringToOkonomiRadConverter<Belop>().convert(jsonString)
        assertThat(okonomiRader2.rader.size).isEqualTo(okonomiRader.rader.size)
        assertThat(okonomiRader2.rader).allMatch { it is Belop }
    }

    @Test
    fun `Mappe BruttoNetto til json og tilbake skal fungere`() {
        val okonomiRader =
            OkonomiRader(
                listOf(
                    BruttoNetto(brutto = 50.0, netto = 40.0),
                    BruttoNetto(brutto = 70.0, netto = 30.0),
                ),
            )

        val json = OkonomiRaderToStringConverter<BruttoNetto>().convert(okonomiRader)
        assertThat(json).contains("50.0").contains("40.0").contains("70.0").contains("30.0")

        val okonomiRader2 = StringToOkonomiRadConverter<BruttoNetto>().convert(json)
        assertThat(okonomiRader2.rader.size).isEqualTo(okonomiRader.rader.size)
        assertThat(okonomiRader2.rader).allMatch { it is BruttoNetto }
    }

    @Test
    fun `Mappe Utbetaling til json og tilbake skal fungere`() {
        val now = LocalDate.now()
        val okonomiRader =
            OkonomiRader(
                listOf(
                    Utbetaling(brutto = 50.0, netto = 40.0, utbetalingsdato = now),
                    Utbetaling(brutto = 70.0, netto = 30.0, periodeTom = now),
                ),
            )

        val json = OkonomiRaderToStringConverter<Utbetaling>().convert(okonomiRader)
        assertThat(json).contains("50.0").contains("40.0").contains("70.0").contains("30.0")
        assertThat(okonomiRader.rader).anyMatch { (it as Utbetaling).utbetalingsdato == now }
        assertThat(okonomiRader.rader).anyMatch { (it as Utbetaling).periodeTom == now }

        val okonomiRader2 = StringToOkonomiRadConverter<Utbetaling>().convert(json)
        assertThat(okonomiRader2.rader.size).isEqualTo(okonomiRader.rader.size)
        assertThat(okonomiRader2.rader).allMatch { it is Utbetaling }
    }

    @Test
    fun `Mappe UtbetalingMedKomponenter til json og tilbake skal fungere`() {
        val okonomiRader =
            OkonomiRader(
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

        val json = OkonomiRaderToStringConverter<UtbetalingMedKomponent>().convert(okonomiRader)
        assertThat(json).contains("50.0").contains("40.0").contains("70.0").contains("30.0").contains("Komponent 1")

        val okonomiRader2 = StringToOkonomiRadConverter<UtbetalingMedKomponent>().convert(json)
        assertThat(okonomiRader2.rader.size).isEqualTo(okonomiRader.rader.size)
        assertThat(okonomiRader2.rader).allMatch { it is UtbetalingMedKomponent }
        assertThat(okonomiRader2.rader).anyMatch { (it as UtbetalingMedKomponent).komponenter.size == 2 }
    }
}
