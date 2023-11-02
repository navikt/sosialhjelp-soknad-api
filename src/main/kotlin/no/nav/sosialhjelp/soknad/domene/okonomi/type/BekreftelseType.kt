package no.nav.sosialhjelp.soknad.domene.okonomi.type

enum class BekreftelseType(tittel: String) {
    BEKREFTELSE_SPARING("inntekt.bankinnskudd"),
    BEKREFTELSE_UTBETALING("inntekt.inntekter"),
    BEKREFTELSE_VERDI("inntekt.eierandeler"),
    BEKREFTELSE_BARNEUTGIFTER("utgifter.barn"),
    BEKREFTELSE_BOUTGIFTER("utgifter.boutgift"),
    BOSTOTTE("inntekt.bostotte"),
    STUDIELAN("inntekt.student");
}

enum class SamtykkeType(tittel: String) {
    BOSTOTTE_SAMTYKKE("inntekt.bostotte.samtykke"),
    UTBETALING_SKATTEETATEN_SAMTYKKE("utbetalinger.skattbar.samtykke");
}

