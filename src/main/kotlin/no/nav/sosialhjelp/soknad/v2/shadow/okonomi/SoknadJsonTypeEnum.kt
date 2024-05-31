package no.nav.sosialhjelp.soknad.v2.shadow.okonomi

// TODO Midlertidig reversed mapping inntil skyggeprod er i prod - etter "gammel kode" fjernes, kan denne fjernes
enum class SoknadJsonTypeEnum(val verdi: String) {
    BARNEBIDRAG("barnebidrag"),
    BOSTOTTE("bostotte"),
    BOSTOTTE_SAMTYKKE("bostotte_samtykke"),
    STUDIELAN("studielanOgStipend"),
    JOBB("jobb"),
    SLUTTOPPGJOER("sluttoppgjoer"),
    BEKREFTELSE_SPARING("sparing"),
    BEKREFTELSE_UTBETALING("utbetaling"),
    BEKREFTELSE_VERDI("verdi"),
    BEKREFTELSE_BARNEUTGIFTER("barneutgifter"),
    BEKREFTELSE_BOUTGIFTER("boutgifter"),
    FORMUE_BRUKSKONTO("brukskonto"),
    FORMUE_BSU("bsu"),
    FORMUE_LIVSFORSIKRING("livsforsikringssparedel"),
    FORMUE_SPAREKONTO("sparekonto"),
    FORMUE_VERDIPAPIRER("verdipapirer"),
    FORMUE_ANNET("belop"),
    VERDI_BOLIG("bolig"),
    VERDI_CAMPINGVOGN("campingvogn"),
    VERDI_KJORETOY("kjoretoy"),
    VERDI_FRITIDSEIENDOM("fritidseiendom"),
    VERDI_ANNET("annet"),
    UTBETALING_SKATTEETATEN("skatteetaten"),
    UTBETALING_SKATTEETATEN_SAMTYKKE("skatteetaten_samtykke"),
    UTBETALING_NAVYTELSE("navytelse"),
    UTBETALING_UTBYTTE("utbytte"),
    UTBETALING_SALG("salg"),
    UTBETALING_FORSIKRING("forsikring"),
    UTBETALING_HUSBANKEN("husbanken"),
    UTBETALING_ANNET("annen"),
    UTGIFTER_BARNEHAGE("barnehage"),
    UTGIFTER_SFO("sfo"),
    UTGIFTER_BARN_FRITIDSAKTIVITETER("barnFritidsaktiviteter"),
    UTGIFTER_BARN_TANNREGULERING("barnTannregulering"),
    UTGIFTER_ANNET_BARN("annenBarneutgift"),
    UTGIFTER_HUSLEIE("husleie"),
    UTGIFTER_STROM("strom"),
    UTGIFTER_KOMMUNAL_AVGIFT("kommunalAvgift"),
    UTGIFTER_OPPVARMING("oppvarming"),
    UTGIFTER_BOLIGLAN_AVDRAG("boliglanAvdrag"),
    UTGIFTER_BOLIGLAN_RENTER("boliglanRenter"),
    UTGIFTER_ANNET_BO("annenBoutgift"),
    UTGIFTER_ANDRE_UTGIFTER("annen"),
    ;

    companion object {
        fun getSoknadJsonType(verdi: String) = entries.firstOrNull { verdi == it.verdi }
    }
}
