package no.nav.sosialhjelp.soknad.v2

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Kategori
import org.junit.jupiter.api.Test

class DiverseTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun whatever() {
        val husleie = mapper.writeValueAsString(Kategori.Husleie)
        val livsopphold = mapper.writeValueAsString(Kategori.Livsopphold)
        val strom = mapper.writeValueAsString(Kategori.StromOgOppvarming)
        val ikkeMat = mapper.writeValueAsString(Kategori.Nodhjelp.IkkeMat)
        val ikkeStrom = mapper.writeValueAsString(Kategori.Nodhjelp.IkkeStrom)
        val ikkeBosted = mapper.writeValueAsString(Kategori.Nodhjelp.IkkeBosted)

        val obj = mapper.readValue(ikkeMat, Kategori::class.java)

        val a = 4
    }
}
