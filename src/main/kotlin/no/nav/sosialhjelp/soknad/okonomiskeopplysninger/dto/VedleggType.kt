package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

enum class VedleggType(
    private val stringName: String
) {
    AnnetAnnet("annet|annet"),
    BarnebidragBetaler("barnebidrag|betaler"),
    BarnebidragMottar("barnebidrag|mottar"),
    DokumentasjonAnnet("dokumentasjon|annet"),
    DokumentasjonAnnetBoutgift("dokumentasjon|annetboutgift"),
    DokumentasjonAnnetInntekter("dokumentasjon|annetinntekter"),
    DokumentasjonAnnetVerdi("dokumentasjon|annetverdi"),
    DokumentasjonCampingvogn("dokumentasjon|campingvogn"),
    DokumentasjonFritidseiendom("dokumentasjon|fritidseiendom"),
    DokumentasjonForsikringsutbetaling("dokumentasjon|forsikringsutbetaling"),
    DokumentasjonKjoretoy("dokumentasjon|kjoretoy"),
    DokumentasjonUtbytte("dokumentasjon|utbytte"),
    FakturaAnnetBarnutgift("faktura|annetbarnutgift"),
    FakturaBarnehage("faktura|barnehage"),
    FakturaFritidsaktivitet("faktura|fritidsaktivitet"),
    FakturaHusleie("faktura|husleie"),
    FakturaKommunaleavgifter("faktura|kommunaleavgifter"),
    FakturaOppvarming("faktura|oppvarming"),
    FakturaSfo("faktura|sfo"),
    FakturaStrom("faktura|strom"),
    FakturaTannbehandling("faktura|tannbehandling"),
    HusbankenVedtak("husbanken|vedtak"),
    HusleiekontraktHusleiekontrakt("husleiekontrakt|husleiekontrakt"),
    HusleiekontraktKommunal("husleiekontrakt|kommunal"),
    KjopekontraktKjopekontrakt("kjopekontrakt|kjopekontrakt"),
    KontooversiktAksjer("kontooversikt|aksjer"),
    KontooversiktAnnet("kontooversikt|annet"),
    KontooversiktBrukskonto("kontooversikt|brukskonto"),
    KontooversiktBsu("kontooversikt|bsu"),
    KontooversiktLivsforsikring("kontooversikt|livsforsikring"),
    KontooversiktSparekonto("kontooversikt|sparekonto"),
    LonnslippArbeid("lonnslipp|arbeid"),
    NedbetalingsplanAvdragslan("nedbetalingsplan|avdraglaan"),
    OppholdstillatelOppholdstillatel("oppholdstillatel|oppholdstillatel"),
    SalgsoppgjorEiendom("salgsoppgjor|eiendom"),
    SamvarsavtaleBarn("samvarsavtale|barn"),
    SkattemeldingSkattemelding("skattemelding|skattemelding"),
    SluttoppgjorArbeid("sluttoppgjor|arbeid"),
    StudentVedtak("student|vedtak");

    override fun toString(): String {
        return stringName
    }

    companion object {
        private val map = VedleggType.values().associateBy(VedleggType::stringName)

        operator fun get(stringName: String): VedleggType = map[stringName]
            ?: throw IllegalArgumentException("Fant ikke VedleggType lik $stringName")
    }
}
