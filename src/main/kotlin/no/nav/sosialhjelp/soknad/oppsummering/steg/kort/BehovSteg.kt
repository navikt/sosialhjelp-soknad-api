package no.nav.sosialhjelp.soknad.oppsummering.steg.kort

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseUtils
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

class BehovSteg {
    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val begrunnelse = jsonInternalSoknad.soknad.data.begrunnelse
        val harUtfyltHvaSokesOm = begrunnelse.hvaSokesOm != null && begrunnelse.hvaSokesOm.isNotEmpty() && !BegrunnelseUtils.isEmptyJson(begrunnelse.hvaSokesOm)

        val situasjonsendring = jsonInternalSoknad.soknad.data.situasjonendring
        val harFyltUtHvaErEndret = !situasjonsendring?.hvaHarEndretSeg.isNullOrBlank()

        return Steg(
            stegNr = 2,
            tittel = "begrunnelsebolk.tittel",
            avsnitt =
                listOf(
                    Avsnitt(
                        tittel = "applikasjon.sidetittel.kortnavn",
                        sporsmal =
                            listOf(
                                Sporsmal(
                                    tittel = "begrunnelse.hva.sporsmal",
                                    erUtfylt = harUtfyltHvaSokesOm,
                                    felt = if (harUtfyltHvaSokesOm) hvaSokerOmFelt(BegrunnelseUtils.jsonToHvaSokesOm(begrunnelse.hvaSokesOm) ?: begrunnelse.hvaSokesOm) else null,
                                ),
                            ),
                    ),
                    Avsnitt(
                        "applikasjon.sidetittel.kortnavn",
                        sporsmal =
                            listOf(
                                Sporsmal("situasjon.kort.hvaErEndret.label", situasjonsendring?.hvaHarEndretSeg?.toFelt(), harFyltUtHvaErEndret),
                            ),
                    ),
                ),
        )
    }

    private fun hvaSokerOmFelt(hvaSokesOm: String): List<Felt> =
        listOf(
            Felt(
                type = Type.TEKST,
                svar = createSvar(hvaSokesOm, SvarType.TEKST),
            ),
        )

    private fun String?.toFelt() =
        this?.let {
            listOf(
                Felt(
                    type = Type.TEKST,
                    svar =
                        createSvar(
                            it,
                            SvarType.TEKST,
                        ),
                ),
            )
        }
}
