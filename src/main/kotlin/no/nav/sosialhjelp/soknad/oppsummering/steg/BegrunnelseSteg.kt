package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

object BegrunnelseSteg {
    fun get(
        jsonInternalSoknad: JsonInternalSoknad,
    ): Steg {
        val begrunnelse = requireNotNull(jsonInternalSoknad.soknad?.data?.begrunnelse)

//        val harUtfyltHvaSokesOm = begrunnelse.hvaSokesOm != null && begrunnelse.hvaSokesOm.isNotEmpty() && !BegrunnelseUtils.isEmptyJson(begrunnelse.hvaSokesOm)

        val harUtfyltHvaSokesOm = !begrunnelse.hvaSokesOm.isNullOrEmpty()

        val hvorforSoke = begrunnelse.hvorforSoke
        val harUtfyltHvorforSoke = !hvorforSoke.isNullOrEmpty()
        return Steg(
            stegNr = 2,
            tittel = "begrunnelsebolk.tittel",
            avsnitt =
                listOf(
                    Avsnitt(
                        tittel = "applikasjon.sidetittel.kortnavn",
                        sporsmal =
                            listOfNotNull(
                                Sporsmal(
                                    tittel = "begrunnelse.hva.sporsmal",
                                    erUtfylt = harUtfyltHvaSokesOm,
//                                    felt = if (harUtfyltHvaSokesOm) hvaSokerOmFelt(BegrunnelseUtils.jsonToHvaSokesOm(begrunnelse.hvaSokesOm) ?: begrunnelse.hvaSokesOm) else null,
                                    felt = if (harUtfyltHvaSokesOm) hvaSokerOmFelt(begrunnelse.hvaSokesOm) else null,
                                ),
                                Sporsmal(
                                    tittel = "begrunnelse.hvorfor.sporsmal",
                                    erUtfylt = harUtfyltHvorforSoke,
                                    felt = if (harUtfyltHvorforSoke) hvorforSokeFelt(requireNotNull(hvorforSoke)) else null,
                                ),
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

    private fun hvorforSokeFelt(hvorforSoke: String): List<Felt> =
        listOf(
            Felt(
                type = Type.TEKST,
                svar = createSvar(hvorforSoke, SvarType.TEKST),
            ),
        )
}
