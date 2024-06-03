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

enum class FormueType(val vedleggKreves: Boolean, val tittelKey: String) : OkonomiType {
    FORMUE_BRUKSKONTO(
        vedleggKreves = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.brukskonto",
    ),
    FORMUE_BSU(
        vedleggKreves = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.bsu",
    ),
    FORMUE_LIVSFORSIKRING(
        vedleggKreves = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.livsforsikring",
    ),
    FORMUE_SPAREKONTO(
        vedleggKreves = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.sparekonto",
    ),
    FORMUE_VERDIPAPIRER(
        vedleggKreves = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.aksjer",
    ),
    FORMUE_ANNET(
        vedleggKreves = true,
        tittelKey = "opplysninger.inntekt.bankinnskudd.annet",
    ),
    VERDI_BOLIG(
        vedleggKreves = false,
        tittelKey = "inntekt.eierandeler.true.type.bolig",
    ),
    VERDI_CAMPINGVOGN(
        vedleggKreves = false,
        tittelKey = "inntekt.eierandeler.true.type.campingvogn",
    ),
    VERDI_KJORETOY(
        vedleggKreves = false,
        tittelKey = "inntekt.eierandeler.true.type.kjoretoy",
    ),
    VERDI_FRITIDSEIENDOM(
        vedleggKreves = false,
        tittelKey = "inntekt.eierandeler.true.type.fritidseiendom",
    ),
    VERDI_ANNET(
        vedleggKreves = false,
        tittelKey = "inntekt.eierandeler.true.type.annet",
    ),
}
