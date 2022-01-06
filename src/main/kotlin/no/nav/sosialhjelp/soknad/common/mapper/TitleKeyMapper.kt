package no.nav.sosialhjelp.soknad.common.mapper

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper

object TitleKeyMapper {
    val soknadTypeToTitleKey: MutableMap<String, String> = HashMap()

    init {
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_HUSLEIE] = "opplysninger.utgifter.boutgift.husleie"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG] = "opplysninger.utgifter.boutgift.avdraglaan.boliglanAvdrag"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER] = "opplysninger.utgifter.boutgift.avdraglaan.boliglanRenter"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_BARNEHAGE] = "opplysninger.utgifter.barn.barnehage"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_SFO] = "opplysninger.utgifter.barn.sfo"
        soknadTypeToTitleKey[SoknadJsonTyper.FORMUE_BRUKSKONTO] = "opplysninger.inntekt.bankinnskudd.brukskonto"
        soknadTypeToTitleKey[SoknadJsonTyper.FORMUE_BSU] = "opplysninger.inntekt.bankinnskudd.bsu"
        soknadTypeToTitleKey[SoknadJsonTyper.FORMUE_SPAREKONTO] = "opplysninger.inntekt.bankinnskudd.sparekonto"
        soknadTypeToTitleKey[SoknadJsonTyper.FORMUE_LIVSFORSIKRING] = "opplysninger.inntekt.bankinnskudd.livsforsikring"
        soknadTypeToTitleKey[SoknadJsonTyper.FORMUE_VERDIPAPIRER] = "opplysninger.inntekt.bankinnskudd.aksjer"
        soknadTypeToTitleKey[SoknadJsonTyper.FORMUE_ANNET] = "opplysninger.inntekt.bankinnskudd.annet"
        soknadTypeToTitleKey[SoknadJsonTyper.VERDI_BOLIG] = "inntekt.eierandeler.true.type.bolig"
        soknadTypeToTitleKey[SoknadJsonTyper.VERDI_CAMPINGVOGN] = "inntekt.eierandeler.true.type.campingvogn"
        soknadTypeToTitleKey[SoknadJsonTyper.VERDI_KJORETOY] = "inntekt.eierandeler.true.type.kjoretoy"
        soknadTypeToTitleKey[SoknadJsonTyper.VERDI_FRITIDSEIENDOM] = "inntekt.eierandeler.true.type.fritidseiendom"
        soknadTypeToTitleKey[SoknadJsonTyper.VERDI_ANNET] = "inntekt.eierandeler.true.type.annet"
        soknadTypeToTitleKey[SoknadJsonTyper.BOSTOTTE] = "opplysninger.inntekt.bostotte"
        soknadTypeToTitleKey[SoknadJsonTyper.JOBB] = "opplysninger.arbeid.jobb"
        soknadTypeToTitleKey[SoknadJsonTyper.STUDIELAN] = "opplysninger.arbeid.student"
        soknadTypeToTitleKey[SoknadJsonTyper.UTBETALING_UTBYTTE] = "opplysninger.inntekt.inntekter.utbytte"
        soknadTypeToTitleKey[SoknadJsonTyper.UTBETALING_SALG] = "opplysninger.inntekt.inntekter.salg"
        soknadTypeToTitleKey[SoknadJsonTyper.UTBETALING_FORSIKRING] = "opplysninger.inntekt.inntekter.forsikringsutbetalinger"
        soknadTypeToTitleKey[SoknadJsonTyper.UTBETALING_ANNET] = "opplysninger.inntekt.inntekter.annet"
        soknadTypeToTitleKey[SoknadJsonTyper.SLUTTOPPGJOER] = "opplysninger.arbeid.avsluttet"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_STROM] = "opplysninger.utgifter.boutgift.strom"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT] = "opplysninger.utgifter.boutgift.kommunaleavgifter"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_OPPVARMING] = "opplysninger.utgifter.boutgift.oppvarming"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_ANNET_BO] = "opplysninger.utgifter.boutgift.andreutgifter"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER] = "opplysninger.utgifter.barn.fritidsaktivitet"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING] = "opplysninger.utgifter.barn.tannbehandling"
        soknadTypeToTitleKey[SoknadJsonTyper.UTGIFTER_ANNET_BARN] = "opplysninger.utgifter.barn.annet"
    }
}
