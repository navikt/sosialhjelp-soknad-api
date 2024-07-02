package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

class SituasjonsendringSteg {
    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val situasjonsendring = jsonInternalSoknad.soknad.data.situasjonendring
        val isUntouched = situasjonsendring == null
        val harFyltUtEndring = situasjonsendring?.harNoeEndretSeg != null
        val harFyltUtHvaErEndret = !situasjonsendring?.hvaHarEndretSeg.isNullOrBlank()

        return Steg(
            stegNr = 3,
            tittel = "situasjon.kort.tittel",
            avsnitt =
                listOf(
                    Avsnitt(
                        "applikasjon.sidetittel.kortnavn",
                        sporsmal =
                            listOf(
                                Sporsmal("situasjon.kort.endring.legend", situasjonsendring?.harNoeEndretSeg?.toFelt(), harFyltUtEndring),
                                Sporsmal("situasjon.kort.hvaErEndret.label", situasjonsendring?.hvaHarEndretSeg?.toFelt(), harFyltUtHvaErEndret),
                            ),
                    ),
                ),
        )
    }

    private fun Boolean?.toFelt() =
        this?.let {
            listOf(
                Felt(
                    type = Type.CHECKBOX,
                    svar =
                        createSvar(
                            it.toLocaleTekst(),
                            SvarType.LOCALE_TEKST,
                        ),
                ),
            )
        }

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

    private fun Boolean.toLocaleTekst() = if (this) "avbryt.ja" else "avbryt.nei"
}
