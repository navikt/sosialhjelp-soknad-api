package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.AnnetAnnet
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.BarnebidragBetaler
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.BarnebidragMottar
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonAnnet
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonAnnetBoutgift
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonAnnetInntekter
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonAnnetVerdi
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonCampingvogn
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonForsikringsutbetaling
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonFritidseiendom
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonKjoretoy
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonUtbytte
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaAnnetBarnutgift
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaBarnehage
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaFritidsaktivitet
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaHusleie
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaKommunaleavgifter
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaOppvarming
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaSfo
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaStrom
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.FakturaTannbehandling
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.HusbankenVedtak
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.HusleiekontraktHusleiekontrakt
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.HusleiekontraktKommunal
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KjopekontraktKjopekontrakt
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktAksjer
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktAnnet
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktBrukskonto
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktBsu
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktLivsforsikring
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktSparekonto
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.LonnslippArbeid
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.NedbetalingsplanAvdragslan
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.OppholdstillatelOppholdstillatel
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.SalgsoppgjorEiendom
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.SamvarsavtaleBarn
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.SkattemeldingSkattemelding
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.SluttoppgjorArbeid
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.StudentVedtak

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

    fun getSoknadPath(vedleggType: VedleggType?): String {
        return when (vedleggType) {
            DokumentasjonAnnetBoutgift, FakturaAnnetBarnutgift, FakturaTannbehandling, FakturaKommunaleavgifter, FakturaFritidsaktivitet, FakturaOppvarming, FakturaStrom, AnnetAnnet -> "opplysningerUtgift"
            BarnebidragBetaler, FakturaSfo, FakturaBarnehage, FakturaHusleie, NedbetalingsplanAvdragslan -> "oversiktUtgift"
            DokumentasjonKjoretoy, DokumentasjonCampingvogn, DokumentasjonFritidseiendom, DokumentasjonAnnetVerdi, KjopekontraktKjopekontrakt, KontooversiktBrukskonto, KontooversiktBsu, KontooversiktSparekonto, KontooversiktLivsforsikring, KontooversiktAksjer, KontooversiktAnnet -> "formue"
            DokumentasjonForsikringsutbetaling, DokumentasjonAnnetInntekter, DokumentasjonUtbytte, DokumentasjonAnnet, SalgsoppgjorEiendom, SluttoppgjorArbeid, HusbankenVedtak -> "utbetaling"
            BarnebidragMottar, LonnslippArbeid, StudentVedtak -> "inntekt"
            else -> throw IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping")
        }
    }

    fun isInSoknadJson(vedleggType: VedleggType?): Boolean {
        return when (vedleggType) {
            OppholdstillatelOppholdstillatel, SamvarsavtaleBarn, HusleiekontraktHusleiekontrakt, HusleiekontraktKommunal, SkattemeldingSkattemelding -> false
            else -> true
        }
    }
}
