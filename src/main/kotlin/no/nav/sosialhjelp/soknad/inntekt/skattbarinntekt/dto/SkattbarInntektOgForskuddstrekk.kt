package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto

data class SkattbarInntektFrontend(
    val inntektFraSkatteetaten: List<SkattbarInntektOgForskuddstrekk>?,
    val inntektFraSkatteetatenFeilet: Boolean?,
    val samtykke: Boolean?,
    val samtykkeTidspunkt: String?
)

data class SkattbarInntektOgForskuddstrekk(
    val organisasjoner: List<Organisasjon>?
)

data class Organisasjon(
    val utbetalinger: List<Utbetaling>?,
    val organisasjonsnavn: String?,
    val orgnr: String?,
    val fom: String,
    val tom: String
)

data class Utbetaling(
    val brutto: Double?,
    val forskuddstrekk: Double?,
    val tittel: String?
)
