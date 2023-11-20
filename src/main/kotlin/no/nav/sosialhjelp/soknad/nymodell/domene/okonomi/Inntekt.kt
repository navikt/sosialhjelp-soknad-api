package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubbles
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface InntektRepository : UpsertRepository<Inntekt>, BubblesRepository<Inntekt>

data class Inntekt(
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val type: InntektType,
    val tittel: String? = null,
    val brutto: Int? = null,
    val netto: Int? = null,
    val utbetaling: Utbetaling? = null,
) : SoknadBubbles(id, soknadId)

data class Utbetaling(
    val kilde: Kilde,
    val orgnummer: String? = null,
    val belop: Int? = null,
    val skattetrekk: Double? = null,
    val andreTrekk: Double? = null,
    val dato: LocalDate? = null,
    val periodeStart: LocalDate? = null,
    val periodeSlutt: LocalDate? = null,
    val komponent: Set<Komponent>? = null // TODO Kan Komponenter være HELT identiske, og bør isåfall være list?
)

data class Komponent(
    val type: String? = null,
    val belop: Double? = null,
    val satsType: String? = null,
    val satsAntall: Double? = null,
    val satsBelop: Double? = null
)

enum class InntektType(
    tittel: String = ""
) : OkonomiType {
    BARNEBIDRAG_MOTTAR,
    DOKUMENTASJON_ANNET_INNTEKTER("opplysninger.inntekt.inntekter.annet"),
    DOKUMENTASJON_FORSIKRINGSUTBETALING("opplysninger.inntekt.inntekter.forsikringsutbetalinger"),
    DOKUMENTASJON_UTBYTTE("opplysninger.inntekt.inntekter.utbytte"),
    HUSBANKEN_VEDTAK("opplysninger.inntekt.bostotte"),
    LONNSLIPP_ARBEID("opplysninger.arbeid.jobb"),
    SALGSOPPGJOR_EIENDOM("opplysninger.inntekt.inntekter.salg"),
    SLUTTOPPGJOR_ARBEID("opplysninger.arbeid.avsluttet"),
    STUDENT_VEDTAK("opplysninger.arbeid.student");
}
