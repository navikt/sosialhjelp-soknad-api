package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

class BegrunnelseSteg {

    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val begrunnelse = jsonInternalSoknad.soknad.data.begrunnelse
        val harUtfyltHvaSokesOm = begrunnelse.hvaSokesOm != null && begrunnelse.hvaSokesOm.isNotEmpty()
        val harUtfyltHvorforSoke = begrunnelse.hvorforSoke != null && begrunnelse.hvorforSoke.isNotEmpty()
        return Steg(
            stegNr = 2,
            tittel = "begrunnelsebolk.tittel",
            avsnitt = listOf(
                Avsnitt(
                    tittel = "applikasjon.sidetittel.kortnavn",
                    sporsmal = listOf(
                        Sporsmal(
                            tittel = "begrunnelse.hva.sporsmal",
                            erUtfylt = harUtfyltHvaSokesOm,
                            felt = if (harUtfyltHvaSokesOm) hvaSokerOmFelt(begrunnelse) else null
                        ),
                        Sporsmal(
                            tittel = "begrunnelse.hvorfor.sporsmal",
                            erUtfylt = harUtfyltHvorforSoke,
                            felt = if (harUtfyltHvorforSoke) hvorforSokeFelt(begrunnelse) else null
                        )

                    )
                )

            )
        )
    }

    private fun hvaSokerOmFelt(begrunnelse: JsonBegrunnelse): List<Felt> {
        return listOf(
            Felt(
                type = Type.TEKST,
                svar = createSvar(begrunnelse.hvaSokesOm, SvarType.TEKST)
            )
        )
    }

    private fun hvorforSokeFelt(begrunnelse: JsonBegrunnelse): List<Felt> {
        return listOf(
            Felt(
                type = Type.TEKST,
                svar = createSvar(begrunnelse.hvorforSoke, SvarType.TEKST)
            )
        )
    }
}
