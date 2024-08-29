package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiElement
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Inntekt(
    override val type: InntektType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val inntektDetaljer: OkonomiDetaljer<OkonomiDetalj> = OkonomiDetaljer(),
) : OkonomiElement

// TODO Tar vare på hvilket Json-objekt de hører til inntil vi får avklart med FSL om vi kan gjøre noe annerledes
enum class InntektType(
    override val dokumentasjonForventet: Boolean,
) : OpplysningType {
    // * * * JsonOkonomioversiktInntekt * * *
    // bruker
    BARNEBIDRAG_MOTTAR(dokumentasjonForventet = true),
    STUDIELAN_INNTEKT(dokumentasjonForventet = true),

    // register - arbeidsforhold
    // TODO se nøyere på hvordan denne henger sammen med henting av register-data, inntekt fra skatteetaten, etc.
    JOBB(dokumentasjonForventet = true),

    // * * * JsonOkonomiopplysningUtbetaling * * *
    // Andre inntekter
    UTBETALING_FORSIKRING(dokumentasjonForventet = true),
    UTBETALING_UTBYTTE(dokumentasjonForventet = true),
    UTBETALING_SALG(dokumentasjonForventet = true),
    UTBETALING_ANNET(dokumentasjonForventet = true),

    // register - arbeidsforhold
    SLUTTOPPGJOER(dokumentasjonForventet = true),

    // register
    UTBETALING_NAVYTELSE(dokumentasjonForventet = false),

    // TODO Denne opprettes hvis inntekt hentes fra skatteetaten - JOBB er typen hvis bruker fyller inn selv.
    // TODO Skal disse 2 synkes på et vis? (ble ikke gjort det tidligere, såvidt jeg kan se)
    UTBETALING_SKATTEETATEN(dokumentasjonForventet = false),
    UTBETALING_HUSBANKEN(dokumentasjonForventet = false),
    ;

    override val group: String
        get() =
            when (this) {
                STUDIELAN_INNTEKT, SLUTTOPPGJOER, JOBB -> "Arbeid"
                BARNEBIDRAG_MOTTAR -> "Familie"
                else -> "Inntekt"
            }
}
