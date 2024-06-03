package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRad
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRader
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Inntekt(
    val type: InntektType,
    @Column("rader")
    val okonomiRader: OkonomiRader<OkonomiRad>? = null,
)

// TODO Tar vare på hvilket Json-objekt de hører til inntil vi får avklart med FSL om vi kan gjøre noe annerledes
enum class InntektType(val vedleggKreves: Boolean) : OkonomiType {
    // JsonOkonomioversiktInntekt
    BARNEBIDRAG_MOTTAR(true),
    JOBB(true),
    STUDIELAN_INNTEKT(true),

    // JsonOkonomiopplysningUtbetaling
    UTBETALING_FORSIKRING(true),
    UTBETALING_ANNET(true),
    UTBETALING_UTBYTTE(true),
    UTBETALING_SALG(true),
    SLUTTOPPGJOER(true),
    UTBETALING_HUSBANKEN(true),
    UTBETALING_SKATTEETATEN(false),
    UTBETALING_NAVYTELSE(false),
}
