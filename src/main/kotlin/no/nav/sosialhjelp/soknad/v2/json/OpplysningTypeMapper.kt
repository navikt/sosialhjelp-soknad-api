package no.nav.sosialhjelp.soknad.v2.json

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.shadow.okonomi.SoknadJsonTypeEnum

private fun OpplysningType.getJsonVerdier(): JsonVerdi =
    when (this) {
        is InntektType -> OpplysningTypeMapper.getJsonVerdier(this)
        is UtgiftType -> OpplysningTypeMapper.getJsonVerdier(this)
        is FormueType -> OpplysningTypeMapper.getJsonVerdier(this)
        is AnnenDokumentasjonType -> OpplysningTypeMapper.getJsonVerdier(this)
        else -> error("Ukjent OpplysningType: $this")
    }

fun OpplysningType.getVedleggTypeString(): String? = getJsonVerdier().vedleggType?.getTypeString()

fun OpplysningType.getVedleggTillegginfoString(): String? = getJsonVerdier().vedleggType?.getTilleggsinfoString()

fun OpplysningType.getSoknadJsonTypeString(): String? = getJsonVerdier().navn?.verdi

// TODO Pågående avklaring med FSL hvor man kanskje slipper denne "2-dimensjonale" mappingen
object OpplysningTypeMapper {
    fun getJsonVerdier(utgiftType: UtgiftType): JsonVerdi =
        when (utgiftType) {
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
            UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BOLIGLAN_AVDRAG, VedleggType.NedbetalingsplanAvdragslan)
            UtgiftType.UTGIFTER_BOLIGLAN_RENTER -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BOLIGLAN_RENTER, null)
            UtgiftType.UTGIFTER_BOLIGLAN -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_BOLIGLAN_AVDRAG, VedleggType.NedbetalingsplanAvdragslan)
            UtgiftType.UTGIFTER_HUSLEIE -> JsonVerdi(SoknadJsonTypeEnum.UTGIFTER_HUSLEIE, VedleggType.FakturaHusleie)
        }

    fun getJsonVerdier(inntektType: InntektType): JsonVerdi =
        when (inntektType) {
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

    fun getJsonVerdier(formueType: FormueType): JsonVerdi =
        when (formueType) {
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

    fun getJsonVerdier(bekreftelseType: BekreftelseType): JsonVerdi =
        when (bekreftelseType) {
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

    fun getJsonVerdier(annenDokumentasjonType: AnnenDokumentasjonType): JsonVerdi =
        when (annenDokumentasjonType) {
            AnnenDokumentasjonType.SKATTEMELDING -> JsonVerdi(null, VedleggType.SkattemeldingSkattemelding)
            AnnenDokumentasjonType.SAMVARSAVTALE -> JsonVerdi(null, VedleggType.SamvarsavtaleBarn)
            AnnenDokumentasjonType.OPPHOLDSTILLATELSE -> JsonVerdi(null, VedleggType.OppholdstillatelOppholdstillatel)
            AnnenDokumentasjonType.HUSLEIEKONTRAKT -> JsonVerdi(null, VedleggType.HusleiekontraktHusleiekontrakt)
            AnnenDokumentasjonType.HUSLEIEKONTRAKT_KOMMUNAL -> JsonVerdi(null, VedleggType.HusleiekontraktKommunal)
            AnnenDokumentasjonType.BEHOV -> JsonVerdi(null, VedleggType.KortBehov)
            AnnenDokumentasjonType.BARNEBIDRAG -> JsonVerdi(null, VedleggType.KortBarnebidrag)
            AnnenDokumentasjonType.BARNEHAGE -> JsonVerdi(null, VedleggType.KortBarnehage)
            AnnenDokumentasjonType.BARNEHAGE_SFO -> JsonVerdi(null, VedleggType.KortBarnehageSFO)
            AnnenDokumentasjonType.BOSTOTTE -> JsonVerdi(null, VedleggType.KortBostotte)
            AnnenDokumentasjonType.HUSLEIE -> JsonVerdi(null, VedleggType.KortHusleie)
            AnnenDokumentasjonType.KONTOOVERSIKT -> JsonVerdi(null, VedleggType.KortKontoroversikt)
            AnnenDokumentasjonType.LONNSLIPP -> JsonVerdi(null, VedleggType.KortLonnslipp)
            AnnenDokumentasjonType.STROM_OPPVARMING -> JsonVerdi(null, VedleggType.KortStromOppvarming)
            AnnenDokumentasjonType.STIPEND_LAN -> JsonVerdi(null, VedleggType.KortStipendLan)
            AnnenDokumentasjonType.ANNET -> JsonVerdi(null, VedleggType.KortAnnet)
        }
}

data class JsonVerdi(
    val navn: SoknadJsonTypeEnum?,
    val vedleggType: VedleggType?,
)
