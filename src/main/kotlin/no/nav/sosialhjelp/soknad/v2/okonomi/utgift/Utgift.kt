package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiOpplysning
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Utgift(
    override val type: UtgiftType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val utgiftDetaljer: OkonomiDetaljer<OkonomiDetalj> = OkonomiDetaljer(),
) : OkonomiOpplysning

enum class UtgiftType(
    override val dokumentasjonForventet: Boolean,
) : OpplysningType {
    // * * * JsonOkonomiopplysningUtgift * * *
    // boutgifter
    UTGIFTER_ANNET_BO(dokumentasjonForventet = true),
    UTGIFTER_KOMMUNAL_AVGIFT(dokumentasjonForventet = true),
    UTGIFTER_OPPVARMING(dokumentasjonForventet = true),
    UTGIFTER_STROM(dokumentasjonForventet = true),

    // barneutgifter
    UTGIFTER_BARN_TANNREGULERING(dokumentasjonForventet = true),
    UTGIFTER_BARN_FRITIDSAKTIVITETER(dokumentasjonForventet = true),
    UTGIFTER_ANNET_BARN(dokumentasjonForventet = true),

    UTGIFTER_ANDRE_UTGIFTER(dokumentasjonForventet = true),

    // * * * JsonOkonomioversiktUtgift * * *
    BARNEBIDRAG_BETALER(dokumentasjonForventet = true),

    // barneutgifter
    UTGIFTER_SFO(dokumentasjonForventet = true),
    UTGIFTER_BARNEHAGE(dokumentasjonForventet = true),

    // boutgift
    UTGIFTER_HUSLEIE(dokumentasjonForventet = true),

    // boutgifter
    // felles håndtering av renter og avdrag fordi de knyttes sammen
    UTGIFTER_BOLIGLAN(dokumentasjonForventet = true),

    UTGIFTER_BOLIGLAN_AVDRAG(dokumentasjonForventet = true),
    UTGIFTER_BOLIGLAN_RENTER(dokumentasjonForventet = false),
    ;

    override val group: VedleggGruppe get() =
        when (this) {
            UTGIFTER_HUSLEIE, UTGIFTER_ANNET_BO -> VedleggGruppe.Bosituasjon
            BARNEBIDRAG_BETALER -> VedleggGruppe.Familie
            UTGIFTER_ANDRE_UTGIFTER -> VedleggGruppe.AndreUtgifter
            else -> VedleggGruppe.Utgifter
        }
}

fun Set<Utgift>.hasType(type: UtgiftType): Boolean = any { it.type == type }
