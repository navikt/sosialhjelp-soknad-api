package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRader
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Formue(
    val type: FormueType,
    @Column("rader")
    val okonomiRader: OkonomiRader<Belop>? = null,
)

enum class FormueType(
    override val dokumentasjonForventet: Boolean,
    override val tittelKey: String,
) : OkonomiType {
    FORMUE_BRUKSKONTO(
        dokumentasjonForventet = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.brukskonto",
    ),
    FORMUE_BSU(
        dokumentasjonForventet = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.bsu",
    ),
    FORMUE_LIVSFORSIKRING(
        dokumentasjonForventet = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.livsforsikring",
    ),
    FORMUE_SPAREKONTO(
        dokumentasjonForventet = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.sparekonto",
    ),
    FORMUE_VERDIPAPIRER(
        dokumentasjonForventet = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.aksjer",
    ),
    FORMUE_ANNET(
        dokumentasjonForventet = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.annet",
    ),
    VERDI_BOLIG(
        dokumentasjonForventet = false,
        tittelKey = "inntekt.eierandeler.true.type.bolig",
    ),
    VERDI_CAMPINGVOGN(
        dokumentasjonForventet = false,
        tittelKey = "inntekt.eierandeler.true.type.campingvogn",
    ),
    VERDI_KJORETOY(
        dokumentasjonForventet = false,
        tittelKey = "inntekt.eierandeler.true.type.kjoretoy",
    ),
    VERDI_FRITIDSEIENDOM(
        dokumentasjonForventet = false,
        tittelKey = "inntekt.eierandeler.true.type.fritidseiendom",
    ),
    VERDI_ANNET(
        dokumentasjonForventet = false,
        tittelKey = "inntekt.eierandeler.true.type.annet",
    ),
}
