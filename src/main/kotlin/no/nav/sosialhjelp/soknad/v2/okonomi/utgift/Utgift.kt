package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRader
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Utgift(
    val type: UtgiftType,
    @Column("rader")
    val okonomiRader: OkonomiRader<Belop>? = null,
)

// TODO Mappingen skal ikke gjøres her - tar kun vare på som referanse inntil videre
enum class UtgiftType(
    override val dokumentasjonForventet: Boolean,
    override val tittelKey: String,
) : OkonomiType {
    // JsonOkonomiopplysningUtgift
    UTGIFTER_ANNET_BO(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_ANNET_BARN(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_BARN_TANNREGULERING(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_KOMMUNAL_AVGIFT(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_BARN_FRITIDSAKTIVITETER(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_OPPVARMING(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_STROM(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_ANDRE_UTGIFTER(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),

    // JsonOkonomioversiktUtgift
    BARNEBIDRAG_BETALER(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_SFO(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_BARNEHAGE(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_HUSLEIE(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_BOLIGLAN_AVDRAG(
        dokumentasjonForventet = true,
        tittelKey = "",
    ),
    UTGIFTER_BOLIGLAN_RENTER(
        dokumentasjonForventet = false,
        tittelKey = "",
    ),
}
