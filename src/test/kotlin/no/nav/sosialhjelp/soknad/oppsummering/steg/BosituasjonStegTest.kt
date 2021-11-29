package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BosituasjonStegTest {

    private val steg = BosituasjonSteg()

    @Test
    fun ikkeUtfyltBotype_ikkeUtfyltAntallPersoner() {
        val soknad = createSoknad(null, null)
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)
        assertThat(res.avsnitt[0].sporsmal).hasSize(2)

        val botypeSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(botypeSporsmal.erUtfylt).isFalse
        assertThat(botypeSporsmal.felt).isNull()

        val antallPersonerSporsmal = res.avsnitt[0].sporsmal[1]
        assertThat(antallPersonerSporsmal.erUtfylt).isFalse
        assertThat(antallPersonerSporsmal.felt).isNull()
    }

    @Test
    fun ikkeUtfyltBotype_utfyltAntallPersoner() {
        val soknad = createSoknad(null, 0)
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)
        assertThat(res.avsnitt[0].sporsmal).hasSize(2)

        val botypeSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(botypeSporsmal.erUtfylt).isFalse
        assertThat(botypeSporsmal.felt).isNull()

        val antallPersonerSporsmal = res.avsnitt[0].sporsmal[1]
        assertThat(antallPersonerSporsmal.erUtfylt).isTrue
        assertThat(antallPersonerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(antallPersonerSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "0")
    }

    @Test
    fun utfyltBotype_ikkeUtfyltAntallPersoner() {
        val soknad = createSoknad(Botype.ANNET, null)
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)
        assertThat(res.avsnitt[0].sporsmal).hasSize(2)

        val botypeSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(botypeSporsmal.erUtfylt).isTrue
        assertThat(botypeSporsmal.felt).hasSize(1)
        validateFeltMedSvar(botypeSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "bosituasjon.annet")

        val antallPersonerSporsmal = res.avsnitt[0].sporsmal[1]
        assertThat(antallPersonerSporsmal.erUtfylt).isFalse
        assertThat(antallPersonerSporsmal.felt).isNull()
    }

    @Test
    fun utfyltBotype_utfyltAntallPersoner() {
        val soknad = createSoknad(Botype.KRISESENTER, 11)
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)
        assertThat(res.avsnitt[0].sporsmal).hasSize(2)

        val botypeSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(botypeSporsmal.erUtfylt).isTrue
        assertThat(botypeSporsmal.felt).hasSize(1)
        validateFeltMedSvar(botypeSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "bosituasjon.annet.botype.krisesenter")

        val antallPersonerSporsmal = res.avsnitt[0].sporsmal[1]
        assertThat(antallPersonerSporsmal.erUtfylt).isTrue
        assertThat(antallPersonerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(antallPersonerSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "11")
    }

    private fun createSoknad(botype: Botype?, antallPersoner: Int?): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withBosituasjon(
                                JsonBosituasjon()
                                    .withBotype(botype)
                                    .withAntallPersoner(antallPersoner)
                            )
                    )
            )
    }
}