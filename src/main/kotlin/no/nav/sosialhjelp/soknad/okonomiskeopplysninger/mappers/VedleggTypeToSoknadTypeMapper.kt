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
    val vedleggTypeToSoknadType: Map<VedleggType, String> =
        mapOf(
            AnnetAnnet to SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER,
            BarnebidragBetaler to SoknadJsonTyper.BARNEBIDRAG,
            BarnebidragMottar to SoknadJsonTyper.BARNEBIDRAG,
            DokumentasjonAnnetBoutgift to SoknadJsonTyper.UTGIFTER_ANNET_BO,
            DokumentasjonAnnetInntekter to SoknadJsonTyper.UTBETALING_ANNET,
            DokumentasjonUtbytte to SoknadJsonTyper.UTBETALING_UTBYTTE,
            DokumentasjonForsikringsutbetaling to SoknadJsonTyper.UTBETALING_FORSIKRING,
            FakturaBarnehage to SoknadJsonTyper.UTGIFTER_BARNEHAGE,
            FakturaAnnetBarnutgift to SoknadJsonTyper.UTGIFTER_ANNET_BARN,
            FakturaFritidsaktivitet to SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER,
            FakturaTannbehandling to SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING,
            FakturaStrom to SoknadJsonTyper.UTGIFTER_STROM,
            FakturaOppvarming to SoknadJsonTyper.UTGIFTER_OPPVARMING,
            FakturaSfo to SoknadJsonTyper.UTGIFTER_SFO,
            FakturaKommunaleavgifter to SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT,
            FakturaHusleie to SoknadJsonTyper.UTGIFTER_HUSLEIE,
            HusbankenVedtak to SoknadJsonTyper.UTBETALING_HUSBANKEN,
            KontooversiktLivsforsikring to SoknadJsonTyper.FORMUE_LIVSFORSIKRING,
            KontooversiktAnnet to SoknadJsonTyper.FORMUE_ANNET,
            KontooversiktBrukskonto to SoknadJsonTyper.FORMUE_BRUKSKONTO,
            KontooversiktAksjer to SoknadJsonTyper.FORMUE_VERDIPAPIRER,
            KontooversiktSparekonto to SoknadJsonTyper.FORMUE_SPAREKONTO,
            KontooversiktBsu to SoknadJsonTyper.FORMUE_BSU,
            LonnslippArbeid to SoknadJsonTyper.JOBB,
            NedbetalingsplanAvdragslan to SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG,
            SalgsoppgjorEiendom to SoknadJsonTyper.UTBETALING_SALG,
            StudentVedtak to SoknadJsonTyper.STUDIELAN,
            SluttoppgjorArbeid to SoknadJsonTyper.SLUTTOPPGJOER,
            // vedleggstypen er også knyttet til soknadstypen "boliglanRenter"
        )

    fun getSoknadPath(vedleggType: VedleggType?): String =
        when (vedleggType) {
            DokumentasjonAnnetBoutgift, FakturaAnnetBarnutgift, FakturaTannbehandling, FakturaKommunaleavgifter,
            FakturaFritidsaktivitet, FakturaOppvarming, FakturaStrom, AnnetAnnet,
            -> "opplysningerUtgift"
            BarnebidragBetaler, FakturaSfo, FakturaBarnehage, FakturaHusleie, NedbetalingsplanAvdragslan -> "oversiktUtgift"
            KontooversiktBrukskonto, KontooversiktBsu, KontooversiktSparekonto, KontooversiktLivsforsikring,
            KontooversiktAksjer, KontooversiktAnnet,
            -> "formue"
            DokumentasjonForsikringsutbetaling, DokumentasjonAnnetInntekter, DokumentasjonUtbytte, SalgsoppgjorEiendom,
            SluttoppgjorArbeid, HusbankenVedtak,
            -> "utbetaling"
            BarnebidragMottar, LonnslippArbeid, StudentVedtak -> "inntekt"
            else -> error("Vedleggstypen eksisterer ikke eller mangler mapping")
        }
}
