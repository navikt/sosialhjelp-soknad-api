package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiElement
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Formue(
    override val type: FormueType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val formueDetaljer: OkonomiDetaljer<Belop> = OkonomiDetaljer(),
) : OkonomiElement

enum class FormueType(
    override val dokumentasjonForventet: Boolean,
) : OpplysningType {
    FORMUE_BRUKSKONTO(dokumentasjonForventet = true),
    FORMUE_BSU(dokumentasjonForventet = true),
    FORMUE_LIVSFORSIKRING(dokumentasjonForventet = true),
    FORMUE_SPAREKONTO(dokumentasjonForventet = true),
    FORMUE_VERDIPAPIRER(dokumentasjonForventet = true),
    FORMUE_ANNET(dokumentasjonForventet = true),
    VERDI_BOLIG(dokumentasjonForventet = false),
    VERDI_CAMPINGVOGN(dokumentasjonForventet = false),
    VERDI_KJORETOY(dokumentasjonForventet = false),
    VERDI_FRITIDSEIENDOM(dokumentasjonForventet = false),
    VERDI_ANNET(dokumentasjonForventet = false),
    ;

    override val group: String get() = "Inntekt"
}
