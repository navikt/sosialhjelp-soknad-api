package no.nav.sosialhjelp.soknad.navenhet.bydel

import io.mockk.every
import io.mockk.spyk
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService.Companion.BYDEL_MARKA_OSLO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BydelFordelingServiceTest {
    companion object {
        private const val TESTVEIEN = "testveien"
        private const val TESTGATEN = "testgaten"

        private const val BYDEL_GRORUD = "030110"
        private const val BYDEL_VESTRE_AKER = "030107"
        private const val BYDEL_NORDRE_AKER = "030108"
    }

    private val bydelFordelingService = spyk(BydelFordelingService())

    @BeforeEach
    internal fun setUp() {
        every { bydelFordelingService getProperty "markaBydelFordeling" } answers { markaBydelFordeling() }
    }

    @Test
    fun skalReturnereBydelTil() {
        val testveien14 = createAdresseForslag(TESTVEIEN, "14")
        assertThat(bydelFordelingService.getBydelTilForMarka(testveien14)).isEqualTo(BYDEL_GRORUD)

        val testgaten1 = createAdresseForslag(TESTGATEN, "1")
        val testgaten100 = createAdresseForslag(TESTGATEN, "100")
        assertThat(bydelFordelingService.getBydelTilForMarka(testgaten1)).isEqualTo(BYDEL_VESTRE_AKER)
        assertThat(bydelFordelingService.getBydelTilForMarka(testgaten100)).isEqualTo(BYDEL_VESTRE_AKER)

        val testgaten101 = createAdresseForslag(TESTGATEN, "101")
        val testgaten899 = createAdresseForslag(TESTGATEN, "899")
        assertThat(bydelFordelingService.getBydelTilForMarka(testgaten101)).isEqualTo(BYDEL_NORDRE_AKER)
        assertThat(bydelFordelingService.getBydelTilForMarka(testgaten899)).isEqualTo(BYDEL_NORDRE_AKER)
    }

    @Test
    fun skalReturnereAdresseforslagGeografiskTilknytningHvisBydelFordelingIkkeFinnes() {
        val adresseForslag = createAdresseForslag("annen adresse", "14")
        val bydelTil = bydelFordelingService.getBydelTilForMarka(adresseForslag)
        assertThat(bydelTil).isEqualTo(BYDEL_MARKA_OSLO)
    }

    private fun createAdresseForslag(
        adresse: String,
        husnummer: String,
    ): AdresseForslag {
        return AdresseForslag(
            adresse,
            husnummer,
            null,
            null,
            null,
            null,
            null,
            BYDEL_MARKA_OSLO,
            null,
            null,
            AdresseForslagType.GATEADRESSE,
        )
    }

    private fun markaBydelFordeling(): List<BydelFordeling> {
        return listOf(
            BydelFordeling(
                TESTVEIEN,
                "gatekode",
                listOf(Husnummerfordeling(1, 9999, HusnummerfordelingType.ALL)),
                BYDEL_MARKA_OSLO,
                BYDEL_GRORUD,
                "Grorud",
            ),
            BydelFordeling(
                TESTGATEN,
                "gatekode",
                listOf(
                    Husnummerfordeling(1, 99, HusnummerfordelingType.ODD),
                    Husnummerfordeling(2, 100, HusnummerfordelingType.EVEN),
                ),
                BYDEL_MARKA_OSLO,
                BYDEL_VESTRE_AKER,
                "Vestre Aker",
            ),
            BydelFordeling(
                TESTGATEN,
                "gatekode",
                listOf(
                    Husnummerfordeling(101, 9999, HusnummerfordelingType.ODD),
                    Husnummerfordeling(102, 9999, HusnummerfordelingType.EVEN),
                ),
                BYDEL_MARKA_OSLO,
                BYDEL_NORDRE_AKER,
                "Nordre Aker",
            ),
        )
    }
}
