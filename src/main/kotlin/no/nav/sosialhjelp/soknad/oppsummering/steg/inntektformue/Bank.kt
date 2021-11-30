package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.addFormueIfPresent
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harValgtFormueType

class Bank {

    fun getAvsnitt(okonomi: JsonOkonomi): Avsnitt {
        val oversikt = okonomi.oversikt
        val opplysninger = okonomi.opplysninger
        return Avsnitt(
            tittel = "opplysninger.formue.bank.undertittel",
            sporsmal = bankSporsmal(oversikt, opplysninger)
        )
    }

    private fun bankSporsmal(oversikt: JsonOkonomioversikt?, opplysninger: JsonOkonomiopplysninger): List<Sporsmal> {
        val harUtfyltBankSporsmal = harBekreftelseTrue(opplysninger, SoknadJsonTyper.BEKREFTELSE_SPARING)
        val sporsmal = ArrayList<Sporsmal>()
        sporsmal.add(
            Sporsmal(
                tittel = "inntekt.bankinnskudd.true.type.sporsmal",
                erUtfylt = true,
                felt = if (harUtfyltBankSporsmal) formueFelter(oversikt) else null
            )
        )
        if (harUtfyltBankSporsmal && harValgtFormueType(oversikt, FORMUE_ANNET)) {
            val beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet
            val harUtfyltAnnetFelt = beskrivelseAvAnnet != null && beskrivelseAvAnnet.sparing != null && beskrivelseAvAnnet.sparing.isNotBlank()
            sporsmal.add(
                Sporsmal(
                    tittel = "inntekt.bankinnskudd.true.type.annet.true.beskrivelse.label",
                    erUtfylt = harUtfyltAnnetFelt,
                    felt = if (harUtfyltAnnetFelt) listOf(
                        Felt(
                            type = Type.TEKST,
                            svar = createSvar(beskrivelseAvAnnet!!.sparing, SvarType.TEKST)
                        )
                    ) else null
                )
            )
        }
        return sporsmal
    }

    private fun formueFelter(oversikt: JsonOkonomioversikt?): List<Felt> {
        val felter = ArrayList<Felt>()
        addFormueIfPresent(oversikt, felter, FORMUE_BRUKSKONTO, "inntekt.bankinnskudd.true.type.brukskonto")
        addFormueIfPresent(oversikt, felter, FORMUE_BSU, "inntekt.bankinnskudd.true.type.bsu")
        addFormueIfPresent(oversikt, felter, FORMUE_LIVSFORSIKRING, "inntekt.bankinnskudd.true.type.livsforsikringssparedel")
        addFormueIfPresent(oversikt, felter, FORMUE_SPAREKONTO, "inntekt.bankinnskudd.true.type.sparekonto")
        addFormueIfPresent(oversikt, felter, FORMUE_VERDIPAPIRER, "inntekt.bankinnskudd.true.type.verdipapirer")
        addFormueIfPresent(oversikt, felter, FORMUE_ANNET, "inntekt.bankinnskudd.true.type.annet")
        return felter
    }
}
