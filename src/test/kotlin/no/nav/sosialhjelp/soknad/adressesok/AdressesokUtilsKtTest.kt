package no.nav.sosialhjelp.soknad.adressesok

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AdressesokUtilsKtTest {

    @Test
    fun skalFormatterKommunenavn() {
        assertThat(formatterKommunenavn(null)).isNull()
        assertThat(formatterKommunenavn("")).isEmpty()
        assertThat(formatterKommunenavn("OSLO")).isEqualTo("Oslo")
        assertThat(formatterKommunenavn("INDRE ØSTFOLD")).isEqualTo("Indre Østfold")
        assertThat(formatterKommunenavn("AURSKOG-HØLAND")).isEqualTo("Aurskog-Høland")
        assertThat(formatterKommunenavn("NORE OG UVDAL")).isEqualTo("Nore og Uvdal")
    }
}