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
enum class UtgiftType(val vedleggKreves: Boolean) : OkonomiType {
    // JsonOkonomiopplysningUtgift
    UTGIFTER_ANNET_BO(true),
    UTGIFTER_ANNET_BARN(true),
    UTGIFTER_BARN_TANNREGULERING(true),
    UTGIFTER_KOMMUNAL_AVGIFT(true),
    UTGIFTER_BARN_FRITIDSAKTIVITETER(true),
    UTGIFTER_OPPVARMING(true),
    UTGIFTER_STROM(true),
    UTGIFTER_ANDRE_UTGIFTER(true),

    // JsonOkonomioversiktUtgift
    BARNEBIDRAG_BETALER(true),
    UTGIFTER_SFO(true),
    UTGIFTER_BARNEHAGE(true),
    UTGIFTER_HUSLEIE(true),
    UTGIFTER_BOLIGLAN_AVDRAG(true),
    UTGIFTER_BOLIGLAN_RENTER(false),
}
