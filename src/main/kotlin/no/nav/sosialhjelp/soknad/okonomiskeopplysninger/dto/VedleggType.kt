package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType

// TODO Legger til midlertidig mapping "tilbake til" opplysningType. Når skyggeprod er ferdig, kan hele denne fjernes
// TODO Fordrer at vi bruker samme type både på "okonomi-poster" og vedlegg i ny modell

enum class VedleggType(
    @JsonValue val stringName: String,
    val opplysningType: OpplysningType?,
) {
    AnnetAnnet("annet|annet", UtgiftType.UTGIFTER_ANDRE_UTGIFTER),
    BarnebidragBetaler("barnebidrag|betaler", UtgiftType.BARNEBIDRAG_BETALER),
    BarnebidragMottar("barnebidrag|mottar", InntektType.BARNEBIDRAG_MOTTAR),

    DokumentasjonAnnetBoutgift("dokumentasjon|annetboutgift", UtgiftType.UTGIFTER_ANNET_BO),
    DokumentasjonAnnetInntekter("dokumentasjon|annetinntekter", InntektType.UTBETALING_ANNET),
    DokumentasjonForsikringsutbetaling("dokumentasjon|forsikringsutbetaling", InntektType.UTBETALING_FORSIKRING),
    DokumentasjonUtbytte("dokumentasjon|utbytte", InntektType.UTBETALING_UTBYTTE),

    FakturaAnnetBarnutgift("faktura|annetbarnutgift", UtgiftType.UTGIFTER_ANNET_BARN),
    FakturaBarnehage("faktura|barnehage", UtgiftType.UTGIFTER_BARNEHAGE),
    FakturaFritidsaktivitet("faktura|fritidsaktivitet", UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER),
    FakturaHusleie("faktura|husleie", UtgiftType.UTGIFTER_HUSLEIE),
    FakturaKommunaleavgifter("faktura|kommunaleavgifter", UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT),
    FakturaOppvarming("faktura|oppvarming", UtgiftType.UTGIFTER_OPPVARMING),
    FakturaSfo("faktura|sfo", UtgiftType.UTGIFTER_SFO),
    FakturaStrom("faktura|strom", UtgiftType.UTGIFTER_STROM),
    FakturaTannbehandling("faktura|tannbehandling", UtgiftType.UTGIFTER_BARN_TANNREGULERING),

    HusbankenVedtak("husbanken|vedtak", InntektType.UTBETALING_HUSBANKEN),

    // populate frontend kategori listen med data den får fra backend

    HusleiekontraktHusleiekontrakt("husleiekontrakt|husleiekontrakt", AnnenDokumentasjonType.HUSLEIEKONTRAKT),
    HusleiekontraktKommunal("husleiekontrakt|kommunal", AnnenDokumentasjonType.HUSLEIEKONTRAKT_KOMMUNAL),

    KontooversiktAksjer("kontooversikt|aksjer", FormueType.FORMUE_VERDIPAPIRER),
    KontooversiktAnnet("kontooversikt|annet", FormueType.FORMUE_ANNET),
    KontooversiktBrukskonto("kontooversikt|brukskonto", FormueType.FORMUE_BRUKSKONTO),
    KontooversiktBsu("kontooversikt|bsu", FormueType.FORMUE_BSU),
    KontooversiktLivsforsikring("kontooversikt|livsforsikring", FormueType.FORMUE_LIVSFORSIKRING),
    KontooversiktSparekonto("kontooversikt|sparekonto", FormueType.FORMUE_SPAREKONTO),

    LonnslippArbeid("lonnslipp|arbeid", InntektType.JOBB),
    NedbetalingsplanAvdragslan("nedbetalingsplan|avdraglaan", UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG),
    OppholdstillatelOppholdstillatel("oppholdstillatel|oppholdstillatel", AnnenDokumentasjonType.OPPHOLDSTILLATELSE),
    SalgsoppgjorEiendom("salgsoppgjor|eiendom", InntektType.UTBETALING_SALG),

    // TODO brukes?
    SamvarsavtaleBarn("samvarsavtale|barn", AnnenDokumentasjonType.SAMVARSAVTALE),
    SkattemeldingSkattemelding("skattemelding|skattemelding", AnnenDokumentasjonType.SKATTEMELDING),
    SluttoppgjorArbeid("sluttoppgjor|arbeid", InntektType.SLUTTOPPGJOER),
    StudentVedtak("student|vedtak", InntektType.STUDIELAN_INNTEKT),
    KortBehov("kort|behov", AnnenDokumentasjonType.BEHOV),
    BarnebidragBarnebidrag("barnebidrag|barnebidrag", AnnenDokumentasjonType.BARNEBIDRAG),

    // Brukes ikke foreløpig
    KortSituasjonsendring("kort|situasjonsendring", null),
    ;

    override fun toString(): String = stringName

    fun getTypeString() = stringName.substring(0, stringName.indexOf('|'))

    fun getTilleggsinfoString() = stringName.substring(stringName.indexOf('|') + 1)

    companion object {
        private val map = entries.associateBy(VedleggType::stringName)

        operator fun get(stringName: String): VedleggType =
            map[stringName]
                ?: throw IllegalArgumentException("Fant ikke VedleggType lik $stringName")
    }
}
