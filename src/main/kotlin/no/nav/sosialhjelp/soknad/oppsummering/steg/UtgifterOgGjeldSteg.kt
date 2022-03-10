package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.harSystemRegistrerteBarn

class UtgifterOgGjeldSteg {
    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val okonomi = jsonInternalSoknad.soknad.data.okonomi
        val forsorgerplikt = jsonInternalSoknad.soknad.data.familie.forsorgerplikt
        val boutgifterSporsmal = boutgifter(okonomi)
        val barneutgifterSporsmal = barneutgifter(okonomi)
        val alleSporsmal = ArrayList(boutgifterSporsmal)
        if (harBarn(forsorgerplikt)) {
            alleSporsmal.addAll(barneutgifterSporsmal)
        }
        return Steg(
            stegNr = 7,
            tittel = "utgifterbolk.tittel",
            avsnitt = listOf(
                Avsnitt(
                    tittel = "utgifter.tittel",
                    sporsmal = alleSporsmal
                )
            )
        )
    }

    private fun boutgifter(okonomi: JsonOkonomi): List<Sporsmal> {
        val boutgiftBekreftelser = okonomi.opplysninger.bekreftelse.filter { BEKREFTELSE_BOUTGIFTER == it.type }
        val erBoutgifterUtfylt = boutgiftBekreftelser.isNotEmpty() && boutgiftBekreftelser[0].verdi != null
        val harBoutgifter = erBoutgifterUtfylt && boutgiftBekreftelser[0].verdi == java.lang.Boolean.TRUE
        val sporsmalList = ArrayList<Sporsmal>()
        sporsmalList.add(
            Sporsmal(
                tittel = "utgifter.boutgift.sporsmal",
                erUtfylt = erBoutgifterUtfylt,
                felt = if (erBoutgifterUtfylt) StegUtils.booleanVerdiFelt(
                    harBoutgifter,
                    "utgifter.boutgift.true",
                    "utgifter.boutgift.false"
                ) else null
            )
        )
        if (erBoutgifterUtfylt && harBoutgifter) {
            val utgifter = okonomi.opplysninger.utgift
            val oversiktUtgift = okonomi.oversikt.utgift
            val felter = ArrayList<Felt>()
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, UTGIFTER_HUSLEIE, "utgifter.boutgift.true.type.husleie")
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_STROM, "utgifter.boutgift.true.type.strom")
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_KOMMUNAL_AVGIFT, "utgifter.boutgift.true.type.kommunalAvgift")
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_OPPVARMING, "utgifter.boutgift.true.type.oppvarming")
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, UTGIFTER_BOLIGLAN_AVDRAG, "utgifter.boutgift.true.type.boliglanAvdrag")
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_ANNET_BO, "utgifter.boutgift.true.type.annenBoutgift")
            sporsmalList.add(
                Sporsmal(
                    tittel = "utgifter.boutgift.true.type.sporsmal",
                    erUtfylt = true,
                    felt = felter
                )
            )
        }
        return sporsmalList
    }

    private fun harBarn(forsorgerplikt: JsonForsorgerplikt): Boolean {
        return harSystemRegistrerteBarn(forsorgerplikt)
    }

    private fun barneutgifter(okonomi: JsonOkonomi): List<Sporsmal> {
        val barneutgiftBekreftelser = okonomi.opplysninger.bekreftelse.filter { BEKREFTELSE_BARNEUTGIFTER == it.type }
        val erBarneutgifterUtfylt = barneutgiftBekreftelser.isNotEmpty() && barneutgiftBekreftelser[0].verdi != null
        val harBarneutgifter = erBarneutgifterUtfylt && barneutgiftBekreftelser[0].verdi == java.lang.Boolean.TRUE
        val sporsmalList = ArrayList<Sporsmal>()
        sporsmalList.add(
            Sporsmal(
                tittel = "utgifter.barn.sporsmal",
                erUtfylt = erBarneutgifterUtfylt,
                felt = if (erBarneutgifterUtfylt) StegUtils.booleanVerdiFelt(
                    harBarneutgifter,
                    "utgifter.barn.true",
                    "utgifter.barn.false"
                ) else null
            )
        )
        if (erBarneutgifterUtfylt && harBarneutgifter) {
            val utgifter = okonomi.opplysninger.utgift
            val oversiktUtgifter = okonomi.oversikt.utgift
            val felter = ArrayList<Felt>()
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_BARN_FRITIDSAKTIVITETER, "utgifter.barn.true.utgifter.barnFritidsaktiviteter")
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, UTGIFTER_BARNEHAGE, "utgifter.barn.true.utgifter.barnehage")
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, UTGIFTER_SFO, "utgifter.barn.true.utgifter.sfo")
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_BARN_TANNREGULERING, "utgifter.barn.true.utgifter.barnTannregulering")
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_ANNET_BARN, "utgifter.barn.true.utgifter.annenBarneutgift")
            sporsmalList.add(
                Sporsmal(
                    tittel = "utgifter.barn.true.utgifter.sporsmal",
                    erUtfylt = true,
                    felt = felter
                )
            )
        }
        return sporsmalList
    }

    private fun addOpplysningUtgiftIfPresent(
        felter: MutableList<Felt>,
        utgifter: List<JsonOkonomiOpplysningUtgift>,
        type: String,
        key: String
    ) {
        // "strom", "kommunalAvgift", "oppvarming", "annenBoutgift", "barnFritidsaktiviteter", "barnTannregulering", “annenBarneutgift” og "annen"
        utgifter.firstOrNull { type == it.type }
            ?.let {
                felter.add(
                    Felt(
                        type = Type.CHECKBOX,
                        svar = createSvar(key, SvarType.LOCALE_TEKST)
                    )
                )
            }
    }

    private fun addOversiktUtgiftIfPresent(
        felter: MutableList<Felt>,
        utgifter: List<JsonOkonomioversiktUtgift>,
        type: String,
        key: String
    ) {
        // "barnebidrag", "husleie", "boliglanAvdrag", "boliglanRenter", “barnehage” og "sfo"
        utgifter.firstOrNull { type == it.type }
            ?.let {
                felter.add(
                    Felt(
                        type = Type.CHECKBOX,
                        svar = createSvar(key, SvarType.LOCALE_TEKST)
                    )
                )
            }
    }
}
