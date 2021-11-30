package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.booleanVerdiFelt
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.addFormueIfPresent
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harValgtFormueType

class AnnenFormue {

    fun getAvsnitt(okonomi: JsonOkonomi): Avsnitt {
        val oversikt = okonomi.oversikt
        val opplysninger = okonomi.opplysninger
        return Avsnitt(
            tittel = "opplysninger.formue.annen.undertittel",
            sporsmal = annenFormueSporsmal(oversikt, opplysninger)
        )
    }

    private fun annenFormueSporsmal(
        oversikt: JsonOkonomioversikt?,
        opplysninger: JsonOkonomiopplysninger
    ): List<Sporsmal> {
        val harUtfyltAnnenFormueSporsmal = harBekreftelse(opplysninger, BEKREFTELSE_VERDI)
        val harSvartJaAnnenFormue = harBekreftelseTrue(opplysninger, BEKREFTELSE_VERDI)
        val sporsmal = ArrayList<Sporsmal>()
        sporsmal.add(
            Sporsmal(
                tittel = "inntekt.eierandeler.sporsmal",
                erUtfylt = harUtfyltAnnenFormueSporsmal,
                felt = if (harUtfyltAnnenFormueSporsmal) booleanVerdiFelt(
                    harSvartJaAnnenFormue,
                    "inntekt.eierandeler.true",
                    "inntekt.eierandeler.false"
                ) else null
            )
        )
        if (harSvartJaAnnenFormue) {
            val harSvartHvaEierDuSporsmal = oversikt?.formue?.any { formueTyper.contains(it.type) } ?: false
            sporsmal.add(
                Sporsmal(
                    tittel = "inntekt.eierandeler.true.type.sporsmal",
                    erUtfylt = harSvartHvaEierDuSporsmal,
                    felt = if (harSvartHvaEierDuSporsmal) annenFormueFelter(oversikt) else null
                )
            )
            if (harValgtFormueType(oversikt, VERDI_ANNET)) {
                val beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet
                val harUtfyltAnnetFelt =
                    beskrivelseAvAnnet != null && beskrivelseAvAnnet.verdi != null && beskrivelseAvAnnet.verdi.isNotBlank()
                sporsmal.add(
                    Sporsmal(
                        tittel = "inntekt.eierandeler.true.type.annet.true.beskrivelse.label",
                        erUtfylt = harUtfyltAnnetFelt,
                        felt = if (harUtfyltAnnetFelt) listOf(
                            Felt(
                                type = Type.TEKST,
                                svar = createSvar(beskrivelseAvAnnet!!.verdi, SvarType.TEKST)
                            )
                        ) else null
                    )
                )
            }
        }
        return sporsmal
    }

    private fun annenFormueFelter(oversikt: JsonOkonomioversikt?): List<Felt> {
        val felter = ArrayList<Felt>()
        addFormueIfPresent(oversikt, felter, VERDI_BOLIG, "inntekt.eierandeler.true.type.bolig")
        addFormueIfPresent(oversikt, felter, VERDI_CAMPINGVOGN, "inntekt.eierandeler.true.type.campingvogn")
        addFormueIfPresent(oversikt, felter, VERDI_KJORETOY, "inntekt.eierandeler.true.type.kjoretoy")
        addFormueIfPresent(oversikt, felter, VERDI_FRITIDSEIENDOM, "inntekt.eierandeler.true.type.fritidseiendom")
        addFormueIfPresent(oversikt, felter, VERDI_ANNET, "inntekt.eierandeler.true.type.annet")
        return felter
    }

    companion object {
        private val formueTyper = listOf(
            VERDI_BOLIG,
            VERDI_CAMPINGVOGN,
            VERDI_KJORETOY,
            VERDI_FRITIDSEIENDOM,
            VERDI_ANNET
        )
    }
}
