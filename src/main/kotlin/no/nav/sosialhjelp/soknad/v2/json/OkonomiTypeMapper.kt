package no.nav.sosialhjelp.soknad.v2.json

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.shadow.okonomi.SoknadJsonTypeEnum

// TODO Pågående avklaring med FSL hvor man kanskje slipper denne "2-dimensjonale" mappingen
object OkonomiTypeMapper {
    fun getJsonVerdier(utgiftType: UtgiftType): JsonVerdi {
        return when (utgiftType) {
            // JsonOpplysningUtgift
            UtgiftType.UTGIFTER_ANDRE_UTGIFTER -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_ANNET, VedleggType.AnnetAnnet)
            UtgiftType.UTGIFTER_ANNET_BO -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_ANNET_BO, VedleggType.DokumentasjonAnnetBoutgift)
            UtgiftType.UTGIFTER_ANNET_BARN -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_ANNET_BARN, VedleggType.FakturaAnnetBarnutgift)
            UtgiftType.UTGIFTER_BARN_TANNREGULERING -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BARN_TANNREGULERING, VedleggType.FakturaTannbehandling)
            UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_KOMMUNAL_AVGIFT, VedleggType.FakturaKommunaleavgifter)
            UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BARN_FRITIDSAKTIVITETER, VedleggType.FakturaFritidsaktivitet)
            UtgiftType.UTGIFTER_OPPVARMING -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_OPPVARMING, VedleggType.FakturaOppvarming)
            UtgiftType.UTGIFTER_STROM -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_STROM, VedleggType.FakturaStrom)
            // JsonOversiktUtgift
            UtgiftType.BARNEBIDRAG_BETALER -> JsonVerdi(SoknadJsonTypeEnum.BARNEBIDRAG, VedleggType.BarnebidragBetaler)
            UtgiftType.UTGIFTER_SFO -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_SFO, VedleggType.FakturaSfo)
            UtgiftType.UTGIFTER_BARNEHAGE -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BARNEHAGE, VedleggType.FakturaBarnehage)
            UtgiftType.UTGIFTER_HUSLEIE -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_HUSLEIE, VedleggType.FakturaHusleie)
            UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BOLIGLAN_AVDRAG, VedleggType.NedbetalingsplanAvdragslan)
            UtgiftType.UTGIFTER_BOLIGLAN_RENTER -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BOLIGLAN_RENTER, null)
        }
    }

    fun getJsonVerdier(inntektType: InntektType): JsonVerdi {
        return when (inntektType) {
            // JsonOversiktInntekt
            InntektType.BARNEBIDRAG_MOTTAR -> JsonVerdi(SoknadJsonTypeEnum.BARNEBIDRAG, VedleggType.BarnebidragMottar)
            InntektType.JOBB -> JsonVerdi(SoknadJsonTypeEnum.JOBB, VedleggType.LonnslippArbeid)
            InntektType.STUDIELAN_INNTEKT -> JsonVerdi(SoknadJsonTypeEnum.STUDIELAN, VedleggType.StudentVedtak)
            // JsonOpplysningUtbetaling
            InntektType.UTBETALING_FORSIKRING -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_FORSIKRING, VedleggType.DokumentasjonForsikringsutbetaling)
            InntektType.UTBETALING_ANNET -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_ANNET, VedleggType.DokumentasjonAnnetInntekter)
            InntektType.UTBETALING_UTBYTTE -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_UTBYTTE, VedleggType.DokumentasjonUtbytte)
            InntektType.UTBETALING_SALG -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_SALG, VedleggType.SalgsoppgjorEiendom)
            InntektType.SLUTTOPPGJOER -> JsonVerdi(SoknadJsonTypeEnum.SLUTTOPPGJOER, VedleggType.SluttoppgjorArbeid)
            InntektType.UTBETALING_HUSBANKEN -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_HUSBANKEN, VedleggType.HusbankenVedtak)
            InntektType.UTBETALING_SKATTEETATEN -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_SKATTEETATEN, null)
            InntektType.UTBETALING_NAVYTELSE -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_NAVYTELSE, null)
        }
    }

    fun getJsonVerdier(formueType: FormueType): JsonVerdi {
        return when (formueType) {
            FormueType.FORMUE_BRUKSKONTO -> JsonVerdi(SoknadJsonTypeEnum.FORMUE_BRUKSKONTO, VedleggType.KontooversiktBrukskonto)
            FormueType.FORMUE_BSU -> JsonVerdi(SoknadJsonTypeEnum.FORMUE_BSU, VedleggType.KontooversiktBsu)
            FormueType.FORMUE_LIVSFORSIKRING -> JsonVerdi(SoknadJsonTypeEnum.FORMUE_LIVSFORSIKRING, VedleggType.KontooversiktLivsforsikring)
            FormueType.FORMUE_SPAREKONTO -> JsonVerdi(SoknadJsonTypeEnum.FORMUE_SPAREKONTO, VedleggType.KontooversiktSparekonto)
            FormueType.FORMUE_VERDIPAPIRER -> JsonVerdi(SoknadJsonTypeEnum.FORMUE_VERDIPAPIRER, VedleggType.KontooversiktAksjer)
            FormueType.FORMUE_ANNET -> JsonVerdi(SoknadJsonTypeEnum.FORMUE_ANNET, VedleggType.KontooversiktAnnet)
            FormueType.VERDI_BOLIG -> JsonVerdi(SoknadJsonTypeEnum.VERDI_BOLIG, null)
            FormueType.VERDI_CAMPINGVOGN -> JsonVerdi(SoknadJsonTypeEnum.VERDI_CAMPINGVOGN, null)
            FormueType.VERDI_KJORETOY -> JsonVerdi(SoknadJsonTypeEnum.VERDI_KJORETOY, null)
            FormueType.VERDI_FRITIDSEIENDOM -> JsonVerdi(SoknadJsonTypeEnum.VERDI_FRITIDSEIENDOM, null)
            FormueType.VERDI_ANNET -> JsonVerdi(SoknadJsonTypeEnum.VERDI_ANNET, null)
        }
    }

    fun getJsonVerdier(bekreftelseType: BekreftelseType): JsonVerdi {
        return when (bekreftelseType) {
            BekreftelseType.BEKREFTELSE_UTBETALING -> JsonVerdi(SoknadJsonTypeEnum.BEKREFTELSE_UTBETALING, null)
            BekreftelseType.BEKREFTELSE_BOUTGIFTER -> JsonVerdi(SoknadJsonTypeEnum.BEKREFTELSE_BOUTGIFTER, null)
            BekreftelseType.BEKREFTELSE_BARNEUTGIFTER -> JsonVerdi(SoknadJsonTypeEnum.BEKREFTELSE_BARNEUTGIFTER, null)
            BekreftelseType.BEKREFTELSE_VERDI -> JsonVerdi(SoknadJsonTypeEnum.BEKREFTELSE_VERDI, null)
            BekreftelseType.STUDIELAN_BEKREFTELSE -> JsonVerdi(SoknadJsonTypeEnum.STUDIELAN, null)
            BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE -> JsonVerdi(SoknadJsonTypeEnum.UTBETALING_SKATTEETATEN_SAMTYKKE, null)
            BekreftelseType.BOSTOTTE_SAMTYKKE -> JsonVerdi(SoknadJsonTypeEnum.BOSTOTTE_SAMTYKKE, null)
            BekreftelseType.BOSTOTTE -> JsonVerdi(SoknadJsonTypeEnum.BOSTOTTE, null)
            BekreftelseType.BEKREFTELSE_SPARING -> JsonVerdi(SoknadJsonTypeEnum.BEKREFTELSE_SPARING, null)
        }
    }
}

data class JsonVerdi(
    val navn: SoknadJsonTypeEnum,
    val vedleggType: VedleggType?,
)
