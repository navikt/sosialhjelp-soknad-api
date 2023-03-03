package no.nav.sosialhjelp.soknad.adressesok.sok

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.adressesok.sok.AdresseStringSplitter.toSokedata
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AdresseStringSplitterTest {

    @Test
    fun tomStrengGirBlanktSvar() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "")
        assertThat(sokedata!!.adresse).isBlank
    }

    @Test
    fun nullStrengGirNullSvar() {
        val sokedata = toSokedata(kodeverkService = null, adresse = null)
        assertThat(sokedata!!.adresse).isNull()
    }

    @Test
    fun kunAdresseVirker() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
    }

    @Test
    fun husnummer() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.husnummer).isEqualTo("2")
    }

    @Test
    fun kunHusnummer() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "234")
        assertThat(sokedata!!.husnummer).isEqualTo("234")
    }

    @Test
    fun husbokstav() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.husnummer).isEqualTo("2")
        assertThat(sokedata.husbokstav).isEqualTo("G")
    }

    @Test
    fun husnummerOgBokstav() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "212G")
        assertThat(sokedata!!.husnummer).isEqualTo("212")
        assertThat(sokedata.husbokstav).isEqualTo("G")
    }

    @Test
    fun postnummer() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G, 0882")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.husnummer).isEqualTo("2")
        assertThat(sokedata.husbokstav).isEqualTo("G")
        assertThat(sokedata.postnummer).isEqualTo("0882")
    }

    @Test
    fun postnummerMedMellomromFlyttet() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G ,0882")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.husnummer).isEqualTo("2")
        assertThat(sokedata.husbokstav).isEqualTo("G")
        assertThat(sokedata.postnummer).isEqualTo("0882")
    }

    @Test
    fun poststed() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G, 0882 OSLO")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.husnummer).isEqualTo("2")
        assertThat(sokedata.husbokstav).isEqualTo("G")
        assertThat(sokedata.postnummer).isEqualTo("0882")
        assertThat(sokedata.poststed).isEqualTo("OSLO")
    }

    @Test
    fun kunGateOgPostnummer() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "Veivei, 0110 ")
        assertThat(sokedata!!.adresse).isEqualTo("Veivei")
        assertThat(sokedata.postnummer).isEqualTo("0110")
    }

    @Test
    fun kunGateOgPoststed() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "Veivei, OSLO")
        assertThat(sokedata!!.adresse).isEqualTo("Veivei")
        assertThat(sokedata.poststed).isEqualTo("OSLO")
    }

    @Test
    fun dobbeltnavnPlussDiverseMellomrom() {
        val sokedata = toSokedata(
            kodeverkService = null,
            adresse = "    Nedre Glommas    Vei   211G  ,  0882  ØVRE OSLO   "
        )
        assertThat(sokedata!!.adresse).isEqualTo("Nedre Glommas Vei")
        assertThat(sokedata.husnummer).isEqualTo("211")
        assertThat(sokedata.husbokstav).isEqualTo("G")
        assertThat(sokedata.postnummer).isEqualTo("0882")
        assertThat(sokedata.poststed).isEqualTo("ØVRE OSLO")
    }

    @Test
    fun kompakt() {
        val sokedata = toSokedata(kodeverkService = null, adresse = " Nedre Glommas Vei211G,0882ØVRE OSLO   ")
        assertThat(sokedata!!.adresse).isEqualTo("Nedre Glommas Vei")
        assertThat(sokedata.husnummer).isEqualTo("211")
        assertThat(sokedata.husbokstav).isEqualTo("G")
        assertThat(sokedata.postnummer).isEqualTo("0882")
        assertThat(sokedata.poststed).isEqualTo("ØVRE OSLO")
    }

    @Test
    fun poststedUtenPostnummer() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G OSLO")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.husnummer).isEqualTo("2")
        assertThat(sokedata.husbokstav).isEqualTo("G")
        assertThat(sokedata.poststed).isEqualTo("OSLO")
    }

    @Test
    fun utenHusnummer() {
        val sokedata = toSokedata(kodeverkService = null, adresse = "asdf, 0882 OSLO")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.postnummer).isEqualTo("0882")
        assertThat(sokedata.poststed).isEqualTo("OSLO")
    }

    @Test
    fun skalKunneSokeMedKommunenavn() {
        val kodeverkService = mockk<KodeverkService>()
        every { kodeverkService.gjettKommunenummer(any()) } returns "0301"

        val sokedata = toSokedata(kodeverkService = kodeverkService, adresse = "asdf, OSLO")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.poststed).isNull()
        assertThat(sokedata.kommunenummer).isEqualTo("0301")
    }

    @Test
    fun skalFungereMedPoststedSelvMedKodeverk() {
        val kodeverkService = mockk<KodeverkService>()
        every { kodeverkService.gjettKommunenummer(any()) } returns "0301"

        val sokedata = toSokedata(kodeverkService = kodeverkService, adresse = "asdf, 0756 OSLO")
        assertThat(sokedata!!.adresse).isEqualTo("asdf")
        assertThat(sokedata.postnummer).isEqualTo("0756")
        assertThat(sokedata.poststed).isEqualTo("OSLO")
        assertThat(sokedata.kommunenummer).isNull()
    }

    @Test
    fun `postnummer fra 1 til 4 tegn`() {
        var sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G, 0")
        assertThat(sokedata!!.postnummer).isEqualTo("0")

        sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G, 03")
        assertThat(sokedata!!.postnummer).isEqualTo("03")

        sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G, 030")
        assertThat(sokedata!!.postnummer).isEqualTo("030")

        sokedata = toSokedata(kodeverkService = null, adresse = "asdf 2G, 0301")
        assertThat(sokedata!!.postnummer).isEqualTo("0301")
    }
}
