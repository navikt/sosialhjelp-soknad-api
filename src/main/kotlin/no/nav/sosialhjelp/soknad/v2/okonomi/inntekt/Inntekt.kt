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
enum class InntektType(
    override val dokumentasjonForventet: Boolean,
    override val tittelKey: String,
) : OkonomiType {
    // JsonOkonomioversiktInntekt
    BARNEBIDRAG_MOTTAR(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    JOBB(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    STUDIELAN_INNTEKT(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),

    // JsonOkonomiopplysningUtbetaling
    UTBETALING_FORSIKRING(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTBETALING_ANNET(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTBETALING_UTBYTTE(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTBETALING_SALG(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    SLUTTOPPGJOER(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTBETALING_HUSBANKEN(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTBETALING_SKATTEETATEN(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTBETALING_NAVYTELSE(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
}
