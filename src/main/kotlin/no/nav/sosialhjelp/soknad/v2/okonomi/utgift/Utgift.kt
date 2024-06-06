package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiElement
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRader
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Utgift(
    override val type: UtgiftType,
    override val beskrivelse: String? = null,
    @Column("rader")
    val okonomiRader: OkonomiRader<Belop>? = null,
) : OkonomiElement

// TODO Mappingen skal ikke gjøres her - tar kun vare på som referanse inntil videre
enum class UtgiftType(
    override val dokumentasjonForventet: Boolean,
) : OkonomiType {
    // JsonOkonomiopplysningUtgift
    UTGIFTER_ANNET_BO(
        dokumentasjonForventet = true,
    ),
    UTGIFTER_ANNET_BARN(dokumentasjonForventet = true),
    UTGIFTER_BARN_TANNREGULERING(dokumentasjonForventet = true),
    UTGIFTER_KOMMUNAL_AVGIFT(dokumentasjonForventet = true),
    UTGIFTER_BARN_FRITIDSAKTIVITETER(dokumentasjonForventet = true),
    UTGIFTER_OPPVARMING(dokumentasjonForventet = true),
    UTGIFTER_STROM(dokumentasjonForventet = true),
    UTGIFTER_ANDRE_UTGIFTER(dokumentasjonForventet = true),

    // JsonOkonomioversiktUtgift
    BARNEBIDRAG_BETALER(dokumentasjonForventet = true),
    UTGIFTER_SFO(dokumentasjonForventet = true),
    UTGIFTER_BARNEHAGE(dokumentasjonForventet = true),
    UTGIFTER_HUSLEIE(dokumentasjonForventet = true),
    UTGIFTER_BOLIGLAN_AVDRAG(dokumentasjonForventet = true),
    UTGIFTER_BOLIGLAN_RENTER(dokumentasjonForventet = false),
}
