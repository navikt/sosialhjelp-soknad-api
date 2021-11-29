package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BegrunnelseStegTest {

    private val steg = BegrunnelseSteg()

    @Test
    fun nullEmptyBegrunnelse() {
        val soknadUtenBegrunnelse = createSoknad(null, "")
        val res = steg.get(soknadUtenBegrunnelse)
        assertThat(res.avsnitt).hasSize(1)
        assertThat(res.avsnitt[0].sporsmal).hasSize(2)

        val hvaSokesOmSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(hvaSokesOmSporsmal.erUtfylt).isFalse
        assertThat(hvaSokesOmSporsmal.felt).isNull()

        val hvorforSokeSporsmal = res.avsnitt[0].sporsmal[1]
        assertThat(hvorforSokeSporsmal.erUtfylt).isFalse
        assertThat(hvorforSokeSporsmal.felt).isNull()
    }

    @Test
    fun utfyltBegrunnelse() {
        val soknadMedBegrunnelse = createSoknad("hva jeg søker om", "hvorfor")
        val res = steg.get(soknadMedBegrunnelse)

        val hvaSokesOmSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(hvaSokesOmSporsmal.erUtfylt).isTrue
        validateFeltMedSvar(hvaSokesOmSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "hva jeg søker om")

        val hvorforSokeSporsmal = res.avsnitt[0].sporsmal[1]
        assertThat(hvorforSokeSporsmal.erUtfylt).isTrue
        validateFeltMedSvar(hvorforSokeSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "hvorfor")
    }

    private fun createSoknad(hvaSokesOm: String?, hvorforSoke: String): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withBegrunnelse(
                                JsonBegrunnelse()
                                    .withHvaSokesOm(hvaSokesOm)
                                    .withHvorforSoke(hvorforSoke)
                            )
                    )
            )
    }
}