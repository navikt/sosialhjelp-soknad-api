package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiElement
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Utgift(
    override val type: UtgiftType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val utgiftDetaljer: OkonomiDetaljer<OkonomiDetalj> = OkonomiDetaljer(),
) : OkonomiElement

// TODO Mappingen skal ikke gjøres her - tar kun vare på som referanse inntil videre
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
    UTGIFTER_HUSLEIE_KOMMUNAL(dokumentasjonForventet = true),

    // boutgifter
    // felles håndtering av renter og avdrag fordi de knyttes sammen
    UTGIFTER_BOLIGLAN(dokumentasjonForventet = true),

    // TODO trenger/skal disse være 2 ? begge mappes til samme input: se BoutgiftRessurs#setBoutgifter
    // TODO Flytt til Json-mapping
    UTGIFTER_BOLIGLAN_AVDRAG(dokumentasjonForventet = true),
    UTGIFTER_BOLIGLAN_RENTER(dokumentasjonForventet = false),
    ;

    override val group: String get() =
        when (this) {
            UTGIFTER_HUSLEIE, UTGIFTER_ANNET_BO -> "Bosituasjon"
            BARNEBIDRAG_BETALER -> "Familie"
            UTGIFTER_ANDRE_UTGIFTER -> "Andre utgifter"
            else -> "Utgifter"
        }
}

fun Set<Utgift>.hasType(type: UtgiftType): Boolean = any { it.type == type }
