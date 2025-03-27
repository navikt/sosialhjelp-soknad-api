package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiOpplysning
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Inntekt(
    override val type: InntektType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val inntektDetaljer: OkonomiDetaljer<OkonomiDetalj> = OkonomiDetaljer(),
) : OkonomiOpplysning

enum class InntektType(
    override val dokumentasjonForventet: Boolean,
) : OpplysningType {
    // * * * JsonOkonomioversiktInntekt * * *
    // bruker
    BARNEBIDRAG_MOTTAR(dokumentasjonForventet = true),
    STUDIELAN_INNTEKT(dokumentasjonForventet = true),

    // register - arbeidsforhold
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

    UTBETALING_SKATTEETATEN(dokumentasjonForventet = false),
    UTBETALING_HUSBANKEN(dokumentasjonForventet = false),
    ;

    override val group: VedleggGruppe
        get() =
            when (this) {
                STUDIELAN_INNTEKT, SLUTTOPPGJOER, JOBB -> VedleggGruppe.Arbeid
                BARNEBIDRAG_MOTTAR -> VedleggGruppe.Familie
                else -> VedleggGruppe.Inntekt
            }
}
