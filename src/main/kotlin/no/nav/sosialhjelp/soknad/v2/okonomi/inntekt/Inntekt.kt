package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiElement
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRad
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRader
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Inntekt(
    override val type: InntektType,
    override val beskrivelse: String? = null,
    @Column("rader")
    val okonomiRader: OkonomiRader<OkonomiRad>? = null,
) : OkonomiElement

// TODO Tar vare på hvilket Json-objekt de hører til inntil vi får avklart med FSL om vi kan gjøre noe annerledes
enum class InntektType(
    override val dokumentasjonForventet: Boolean,
) : OkonomiType {
    // JsonOkonomioversiktInntekt
    BARNEBIDRAG_MOTTAR(dokumentasjonForventet = true),
    JOBB(dokumentasjonForventet = true),
    STUDIELAN_INNTEKT(dokumentasjonForventet = true),

    // JsonOkonomiopplysningUtbetaling
    UTBETALING_FORSIKRING(dokumentasjonForventet = true),
    UTBETALING_ANNET(dokumentasjonForventet = true),
    UTBETALING_UTBYTTE(dokumentasjonForventet = true),
    UTBETALING_SALG(dokumentasjonForventet = true),
    SLUTTOPPGJOER(dokumentasjonForventet = true),
    UTBETALING_HUSBANKEN(dokumentasjonForventet = true),
    UTBETALING_SKATTEETATEN(dokumentasjonForventet = true),
    UTBETALING_NAVYTELSE(dokumentasjonForventet = true),
}
