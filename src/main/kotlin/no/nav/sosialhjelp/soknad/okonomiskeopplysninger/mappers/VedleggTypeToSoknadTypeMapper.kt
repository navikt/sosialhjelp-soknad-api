package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType

object VedleggTypeToSoknadTypeMapper {
    val vedleggTypeToSoknadType: MutableMap<String, String> = HashMap()

    init {
        vedleggTypeToSoknadType["kontooversikt|aksjer"] = SoknadJsonTyper.FORMUE_VERDIPAPIRER
        vedleggTypeToSoknadType["faktura|annetbarnutgift"] = SoknadJsonTyper.UTGIFTER_ANNET_BARN
        vedleggTypeToSoknadType["dokumentasjon|annetboutgift"] = SoknadJsonTyper.UTGIFTER_ANNET_BO
        vedleggTypeToSoknadType["dokumentasjon|annetinntekter"] = SoknadJsonTyper.UTBETALING_ANNET
        vedleggTypeToSoknadType["dokumentasjon|annetverdi"] = SoknadJsonTyper.VERDI_ANNET // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType["dokumentasjon|campingvogn"] = SoknadJsonTyper.VERDI_CAMPINGVOGN // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType["dokumentasjon|fritidseiendom"] = SoknadJsonTyper.VERDI_FRITIDSEIENDOM // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType["kjopekontrakt|kjopekontrakt"] = SoknadJsonTyper.VERDI_BOLIG // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType["dokumentasjon|kjoretoy"] = SoknadJsonTyper.VERDI_KJORETOY // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType["faktura|barnehage"] = SoknadJsonTyper.UTGIFTER_BARNEHAGE
        vedleggTypeToSoknadType["barnebidrag|betaler"] = SoknadJsonTyper.BARNEBIDRAG
        vedleggTypeToSoknadType["kontooversikt|brukskonto"] = SoknadJsonTyper.FORMUE_BRUKSKONTO
        vedleggTypeToSoknadType["kontooversikt|bsu"] = SoknadJsonTyper.FORMUE_BSU
        vedleggTypeToSoknadType["salgsoppgjor|eiendom"] = SoknadJsonTyper.UTBETALING_SALG
        vedleggTypeToSoknadType["dokumentasjon|forsikringsutbetaling"] = SoknadJsonTyper.UTBETALING_FORSIKRING
        vedleggTypeToSoknadType["faktura|fritidsaktivitet"] = SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
        vedleggTypeToSoknadType["faktura|husleie"] = SoknadJsonTyper.UTGIFTER_HUSLEIE
        vedleggTypeToSoknadType["faktura|kommunaleavgifter"] = SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT
        vedleggTypeToSoknadType["kontooversikt|livsforsikring"] = SoknadJsonTyper.FORMUE_LIVSFORSIKRING
        vedleggTypeToSoknadType["barnebidrag|mottar"] = SoknadJsonTyper.BARNEBIDRAG
        vedleggTypeToSoknadType["faktura|oppvarming"] = SoknadJsonTyper.UTGIFTER_OPPVARMING
        vedleggTypeToSoknadType["faktura|sfo"] = SoknadJsonTyper.UTGIFTER_SFO
        vedleggTypeToSoknadType["kontooversikt|sparekonto"] = SoknadJsonTyper.FORMUE_SPAREKONTO
        vedleggTypeToSoknadType["faktura|strom"] = SoknadJsonTyper.UTGIFTER_STROM
        vedleggTypeToSoknadType["faktura|tannbehandling"] = SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING
        vedleggTypeToSoknadType["dokumentasjon|utbytte"] = SoknadJsonTyper.UTBETALING_UTBYTTE
        vedleggTypeToSoknadType["husbanken|vedtak"] = SoknadJsonTyper.UTBETALING_HUSBANKEN
        vedleggTypeToSoknadType["student|vedtak"] = SoknadJsonTyper.STUDIELAN
        vedleggTypeToSoknadType["lonnslipp|arbeid"] = SoknadJsonTyper.JOBB
        vedleggTypeToSoknadType["sluttoppgjor|arbeid"] = SoknadJsonTyper.SLUTTOPPGJOER
        vedleggTypeToSoknadType["kontooversikt|annet"] = SoknadJsonTyper.FORMUE_ANNET
        vedleggTypeToSoknadType["annet|annet"] = SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER
        vedleggTypeToSoknadType["dokumentasjon|annet"] = SoknadJsonTyper.UTBETALING_ANNET
        vedleggTypeToSoknadType["nedbetalingsplan|avdraglaan"] = SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG // vedleggstypen er også knyttet til soknadstypen "boliglanRenter"
    }

    fun getSoknadPath(vedleggType: String?): String {
        return when (vedleggType) {
            "dokumentasjon|annetboutgift", "faktura|annetbarnutgift", "faktura|tannbehandling", "faktura|kommunaleavgifter", "faktura|fritidsaktivitet", "faktura|oppvarming", "faktura|strom", "annet|annet" -> "opplysningerUtgift"
            "barnebidrag|betaler", "faktura|sfo", "faktura|barnehage", "faktura|husleie", "nedbetalingsplan|avdraglaan" -> "oversiktUtgift"
            "dokumentasjon|kjoretoy", "dokumentasjon|campingvogn", "dokumentasjon|fritidseiendom", "dokumentasjon|annetverdi", "kjopekontrakt|kjopekontrakt", "kontooversikt|brukskonto", "kontooversikt|bsu", "kontooversikt|sparekonto", "kontooversikt|livsforsikring", "kontooversikt|aksjer", "kontooversikt|annet" -> "formue"
            "dokumentasjon|forsikringsutbetaling", "dokumentasjon|annetinntekter", "dokumentasjon|utbytte", "dokumentasjon|annet", "salgsoppgjor|eiendom", "sluttoppgjor|arbeid", "husbanken|vedtak" -> "utbetaling"
            "barnebidrag|mottar", "lonnslipp|arbeid", "student|vedtak" -> "inntekt"
            else -> throw IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping")
        }
    }

    fun isInSoknadJson(vedleggType: VedleggType?): Boolean {
        return when (vedleggType) {
            VedleggType.OppholdstillatelOppholdstillatel, VedleggType.SamvarsavtaleBarn, VedleggType.HusleiekontraktHusleiekontrakt, VedleggType.HusleiekontraktKommunal, VedleggType.SkattemeldingSkattemelding -> false
            else -> true
        }
    }
}
