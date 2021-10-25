package no.nav.sosialhjelp.soknad.client.skatteetaten.dto

data class SkattbarInntekt(
    val oppgaveInntektsmottaker: List<OppgaveInntektsmottaker> = java.util.ArrayList()
)

data class OppgaveInntektsmottaker(
    val kalendermaaned: String? = null,
    val opplysningspliktigId: String? = null,
    val inntekt: List<Inntekt> = ArrayList(),
    val forskuddstrekk: List<Forskuddstrekk> = ArrayList()
)

data class Inntekt(
    val skatteOgAvgiftsregel: String? = null,
    val fordel: String? = null,
    val utloeserArbeidsgiveravgift: Boolean? = null,
    val inngaarIGrunnlagForTrekk: Boolean? = null,
    val beloep: Int? = null,
    val loennsinntekt: Loennsinntekt? = null,
    val ytelseFraOffentlige: YtelseFraOffentlige? = null,
    val pensjonEllerTrygd: PensjonEllerTrygd? = null,
    val lottOgPartInnenFiske: LottOgPartInnenFiske? = null,
    val dagmammaIEgenBolig: DagmammaIEgenBolig? = null,
    val naeringsinntekt: Naeringsinntekt? = null,
    val aldersUfoereEtterlatteAvtalefestetOgKrigspensjon: AldersUfoereEtterlatteAvtalefestetOgKrigspensjon? = null
)

data class Loennsinntekt(
    val tilleggsinformasjon: Tilleggsinformasjon? = null
)

class AldersUfoereEtterlatteAvtalefestetOgKrigspensjon
class DagmammaIEgenBolig
class Naeringsinntekt
class LottOgPartInnenFiske

data class PensjonEllerTrygd(
    val tilleggsinformasjon: Tilleggsinformasjon? = null
)

data class YtelseFraOffentlige(
    val tilleggsinformasjon: Tilleggsinformasjon? = null
)

data class Tilleggsinformasjon(
    val dagmammaIEgenBolig: DagmammaIEgenBolig? = null,
    val lottOgPart: LottOgPartInnenFiske? = null,
    val pensjon: AldersUfoereEtterlatteAvtalefestetOgKrigspensjon? = null
)

data class Forskuddstrekk(
    val beskrivelse: String? = null,
    val beloep: Int? = null
)
