package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

class BosituasjonSteg {

    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val bosituasjon = jsonInternalSoknad.soknad.data.bosituasjon
        return Steg(
            stegNr = 5,
            tittel = "bosituasjonbolk.tittel",
            avsnitt = listOf(
                Avsnitt(
                    tittel = "bosituasjon.tittel",
                    sporsmal = bosituasjonSporsmal(bosituasjon)
                )
            )
        )
    }

    private fun bosituasjonSporsmal(bosituasjon: JsonBosituasjon?): List<Sporsmal> {
        val harUtfyltHvorBorDu = bosituasjon != null && bosituasjon.botype != null
        val harUtfyltHvorMangeBorSammen = bosituasjon != null && bosituasjon.antallPersoner != null
        val hvordanBorDuSporsmal = Sporsmal(
            tittel = "bosituasjon.sporsmal",
            erUtfylt = harUtfyltHvorBorDu,
            felt = if (harUtfyltHvorBorDu) listOf(
                Felt(
                    type = Type.CHECKBOX,
                    svar = createSvar(botypeToTekstKey(bosituasjon!!.botype), SvarType.LOCALE_TEKST)
                )
            ) else null
        )

        val hvorMangeBorSammenSporsmal = Sporsmal(
            tittel = "bosituasjon.antallpersoner.sporsmal",
            erUtfylt = harUtfyltHvorMangeBorSammen,
            felt = if (harUtfyltHvorMangeBorSammen) listOf(
                Felt(
                    type = Type.TEKST,
                    svar = createSvar(bosituasjon!!.antallPersoner.toString(), SvarType.TEKST)
                )
            ) else null
        )
        return listOf(
            hvordanBorDuSporsmal,
            hvorMangeBorSammenSporsmal
        )
    }

    private fun botypeToTekstKey(botype: Botype): String {
        val key: String = when (botype) {
            Botype.EIER -> "bosituasjon.eier"
            Botype.LEIER -> "bosituasjon.leier"
            Botype.KOMMUNAL -> "bosituasjon.kommunal"
            Botype.INGEN -> "bosituasjon.ingen"
            Botype.FORELDRE -> "bosituasjon.annet.botype.foreldre"
            Botype.FAMILIE -> "bosituasjon.annet.botype.familie"
            Botype.VENNER -> "bosituasjon.annet.botype.venner"
            Botype.INSTITUSJON -> "bosituasjon.annet.botype.institusjon"
            Botype.FENGSEL -> "bosituasjon.annet.botype.fengsel"
            Botype.KRISESENTER -> "bosituasjon.annet.botype.krisesenter"
            Botype.ANNET -> "bosituasjon.annet"
            else -> "bosituasjon.annet"
        }
        return key
    }
}
