package no.nav.sosialhjelp.soknad.adressesok.sok

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.adressesok.sok.AdresseStringSplitter.postnummerMatch
import no.nav.sosialhjelp.soknad.adressesok.sok.AdresseStringSplitter.toSokedata
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AdresseStringSplitterTest {

    @Test
    fun tomStrengGirBlanktSvar() {
        assertThat(toSokedata(null, "")!!.adresse).isBlank
    }

    @Test
    fun nullStrengGirNullSvar() {
        assertThat(toSokedata(null, null)!!.adresse).isNull()
    }

    @Test
    fun kunAdresseVirker() {
        assertThat(toSokedata(null, "asdf")!!.adresse).isEqualTo("asdf")
    }

    @Test
    fun husnummer() {
        val result = toSokedata(null, "asdf 2")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.husnummer).isEqualTo("2")
    }

    @Test
    fun kunHusnummer() {
        val result = toSokedata(null, "234")
        assertThat(result!!.husnummer).isEqualTo("234")
    }

    @Test
    fun husbokstav() {
        val result = toSokedata(null, "asdf 2G")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.husnummer).isEqualTo("2")
        assertThat(result.husbokstav).isEqualTo("G")
    }

    @Test
    fun husnummerOgBokstav() {
        val result = toSokedata(null, "212G")
        assertThat(result!!.husnummer).isEqualTo("212")
        assertThat(result.husbokstav).isEqualTo("G")
    }

    @Test
    fun postnummer() {
        val result = toSokedata(null, "asdf 2G, 0882")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.husnummer).isEqualTo("2")
        assertThat(result.husbokstav).isEqualTo("G")
        assertThat(result.postnummer).isEqualTo("0882")
    }

    @Test
    fun postnummerMedMellomromFlyttet() {
        val result = toSokedata(null, "asdf 2G ,0882")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.husnummer).isEqualTo("2")
        assertThat(result.husbokstav).isEqualTo("G")
        assertThat(result.postnummer).isEqualTo("0882")
    }

    @Test
    fun kunPostnummer() {
        val result = toSokedata(null, "0882")
        assertThat(result!!.postnummer).isEqualTo("0882")
    }

    @Test
    fun kunPostnummerMedMellomrom() {
        val result = toSokedata(null, "   0882   ")
        assertThat(result!!.postnummer).isEqualTo("0882")
    }

    @Test
    fun poststed() {
        val result = toSokedata(null, "asdf 2G, 0882 OSLO")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.husnummer).isEqualTo("2")
        assertThat(result.husbokstav).isEqualTo("G")
        assertThat(result.postnummer).isEqualTo("0882")
        assertThat(result.poststed).isEqualTo("OSLO")
    }

    @Test
    fun kunGateOgPostnummer() {
        val result = toSokedata(null, "Veivei, 0110 ")
        assertThat(result!!.adresse).isEqualTo("Veivei")
        assertThat(result.postnummer).isEqualTo("0110")
    }

    @Test
    fun kunGateOgPoststed() {
        val result = toSokedata(null, "Veivei, OSLO")
        assertThat(result!!.adresse).isEqualTo("Veivei")
        assertThat(result.poststed).isEqualTo("OSLO")
    }

    @Test
    fun dobbeltnavnPlussDiverseMellomrom() {
        val result = toSokedata(null, "    Nedre Glommas    Vei   211G  ,  0882  ØVRE OSLO   ")
        assertThat(result!!.adresse).isEqualTo("Nedre Glommas Vei")
        assertThat(result.husnummer).isEqualTo("211")
        assertThat(result.husbokstav).isEqualTo("G")
        assertThat(result.postnummer).isEqualTo("0882")
        assertThat(result.poststed).isEqualTo("ØVRE OSLO")
    }

    @Test
    fun kompakt() {
        val result = toSokedata(null, " Nedre Glommas Vei211G,0882ØVRE OSLO   ")
        assertThat(result!!.adresse).isEqualTo("Nedre Glommas Vei")
        assertThat(result.husnummer).isEqualTo("211")
        assertThat(result.husbokstav).isEqualTo("G")
        assertThat(result.postnummer).isEqualTo("0882")
        assertThat(result.poststed).isEqualTo("ØVRE OSLO")
    }

    @Test
    fun poststedUtenPostnummer() {
        val result = toSokedata(null, "asdf 2G OSLO")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.husnummer).isEqualTo("2")
        assertThat(result.husbokstav).isEqualTo("G")
        assertThat(result.poststed).isEqualTo("OSLO")
    }

    @Test
    fun utenHusnummer() {
        val result = toSokedata(null, "asdf, 0882 OSLO")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.postnummer).isEqualTo("0882")
        assertThat(result.poststed).isEqualTo("OSLO")
    }

    @Test
    fun skalKunneSokeMedKommunenavn() {
        val kodeverkService = mockk<KodeverkService>()
        every { kodeverkService.gjettKommunenummer(any()) } returns "0301"
        val result = toSokedata(kodeverkService, "asdf, OSLO")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.poststed).isNull()
        assertThat(result.kommunenummer).isEqualTo("0301")
    }

    @Test
    fun skalFungereMedPoststedSelvMedKodeverk() {
        val kodeverkService = mockk<KodeverkService>()
        every { kodeverkService.gjettKommunenummer(any()) } returns "0301"
        val result = toSokedata(kodeverkService, "asdf, 0756 OSLO")
        assertThat(result!!.adresse).isEqualTo("asdf")
        assertThat(result.postnummer).isEqualTo("0756")
        assertThat(result.poststed).isEqualTo("OSLO")
        assertThat(result.kommunenummer).isNull()
    }

    @Test
    fun postnummerMatchTest() {
        var sokedata = postnummerMatch("0001")
        assertThat(sokedata?.postnummer).isEqualTo("0001")
        sokedata = postnummerMatch("0001 ")
        assertThat(sokedata?.postnummer).isEqualTo("0001")
        sokedata = postnummerMatch(" 0001")
        assertThat(sokedata?.postnummer).isEqualTo("0001")
        sokedata = postnummerMatch("Haugeveien, 0001 klavestaad")
        assertThat(sokedata).isNull()
        sokedata = postnummerMatch("Sannergata 2")
        assertThat(sokedata).isNull()
        sokedata = postnummerMatch("Sannergata0001")
        assertThat(sokedata).isNull()
        sokedata = postnummerMatch("0001Klavestad")
        assertThat(sokedata).isNull()
        sokedata = postnummerMatch("0001 Klavestad")
        assertThat(sokedata).isNull()
    }

    @Test
    fun `postnummer fra 1 til 4 tegn`() {
        var result = toSokedata(null, "asdf 2G, 0")
        assertThat(result!!.postnummer).isEqualTo("0")

        result = toSokedata(null, "asdf 2G, 03")
        assertThat(result!!.postnummer).isEqualTo("03")

        result = toSokedata(null, "asdf 2G, 030")
        assertThat(result!!.postnummer).isEqualTo("030")

        result = toSokedata(null, "asdf 2G, 0301")
        assertThat(result!!.postnummer).isEqualTo("0301")
    }
}
