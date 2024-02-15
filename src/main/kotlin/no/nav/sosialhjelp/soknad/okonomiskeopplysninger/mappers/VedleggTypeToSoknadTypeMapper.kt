package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.AnnetAnnet
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.BarnebidragBetaler
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.BarnebidragMottar
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonAnnetBoutgift
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonAnnetInntekter
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.DokumentasjonForsikringsutbetaling
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
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktAksjer
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktAnnet
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktBrukskonto
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktBsu
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktLivsforsikring
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.KontooversiktSparekonto
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.LonnslippArbeid
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.NedbetalingsplanAvdragslan
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.SalgsoppgjorEiendom
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.SluttoppgjorArbeid
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType.StudentVedtak

object VedleggTypeToSoknadTypeMapper {
    val vedleggTypeToSoknadType: Map<VedleggType, String> = mapOf(
        KontooversiktAksjer to SoknadJsonTyper.FORMUE_VERDIPAPIRER,
        FakturaAnnetBarnutgift to SoknadJsonTyper.UTGIFTER_ANNET_BARN,
        DokumentasjonAnnetBoutgift to SoknadJsonTyper.UTGIFTER_ANNET_BO,
        DokumentasjonAnnetInntekter to SoknadJsonTyper.UTBETALING_ANNET,
        FakturaBarnehage to SoknadJsonTyper.UTGIFTER_BARNEHAGE,
        BarnebidragBetaler to SoknadJsonTyper.BARNEBIDRAG,
        KontooversiktBrukskonto to SoknadJsonTyper.FORMUE_BRUKSKONTO,
        KontooversiktBsu to SoknadJsonTyper.FORMUE_BSU,
        SalgsoppgjorEiendom to SoknadJsonTyper.UTBETALING_SALG,
        DokumentasjonForsikringsutbetaling to SoknadJsonTyper.UTBETALING_FORSIKRING,
        FakturaFritidsaktivitet to SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER,
        FakturaHusleie to SoknadJsonTyper.UTGIFTER_HUSLEIE,
        FakturaKommunaleavgifter to SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT,
        KontooversiktLivsforsikring to SoknadJsonTyper.FORMUE_LIVSFORSIKRING,
        BarnebidragMottar to SoknadJsonTyper.BARNEBIDRAG,
        FakturaOppvarming to SoknadJsonTyper.UTGIFTER_OPPVARMING,
        FakturaSfo to SoknadJsonTyper.UTGIFTER_SFO,
        KontooversiktSparekonto to SoknadJsonTyper.FORMUE_SPAREKONTO,
        FakturaStrom to SoknadJsonTyper.UTGIFTER_STROM,
        FakturaTannbehandling to SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING,
        DokumentasjonUtbytte to SoknadJsonTyper.UTBETALING_UTBYTTE,
        HusbankenVedtak to SoknadJsonTyper.UTBETALING_HUSBANKEN,
        StudentVedtak to SoknadJsonTyper.STUDIELAN,
        LonnslippArbeid to SoknadJsonTyper.JOBB,
        SluttoppgjorArbeid to SoknadJsonTyper.SLUTTOPPGJOER,
        KontooversiktAnnet to SoknadJsonTyper.FORMUE_ANNET,
        AnnetAnnet to SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER,
        // vedleggstypen er også knyttet til soknadstypen "boliglanRenter"
        NedbetalingsplanAvdragslan to SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
    )

    fun getSoknadPath(vedleggType: VedleggType?): String = when (vedleggType) {
        DokumentasjonAnnetBoutgift, FakturaAnnetBarnutgift, FakturaTannbehandling, FakturaKommunaleavgifter, FakturaFritidsaktivitet, FakturaOppvarming, FakturaStrom, AnnetAnnet -> "opplysningerUtgift"
        BarnebidragBetaler, FakturaSfo, FakturaBarnehage, FakturaHusleie, NedbetalingsplanAvdragslan -> "oversiktUtgift"
        KontooversiktBrukskonto, KontooversiktBsu, KontooversiktSparekonto, KontooversiktLivsforsikring, KontooversiktAksjer, KontooversiktAnnet -> "formue"
        DokumentasjonForsikringsutbetaling, DokumentasjonAnnetInntekter, DokumentasjonUtbytte, SalgsoppgjorEiendom, SluttoppgjorArbeid, HusbankenVedtak -> "utbetaling"
        BarnebidragMottar, LonnslippArbeid, StudentVedtak -> "inntekt"
        else -> error("Vedleggstypen eksisterer ikke eller mangler mapping")
    }
}
