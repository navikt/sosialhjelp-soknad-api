package no.nav.sosialhjelp.soknad.v2.familie

import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
class FamilieService(private val familieRepository: FamilieRepository) {
    fun findFamilie(soknadId: UUID): Familie? {
        return familieRepository.findById(soknadId).getOrNull()
    }

    fun updateForsorger(soknadId: UUID, barnebidrag: Barnebidrag?, ansvar: List<Barn>): Familie =
        familieRepository.findById(soknadId).getOrNull()?.let { familie ->
            val updated = familie.copy(
                barnebidrag = barnebidrag,
                ansvar = familie.ansvar.map { (uuid, existing) ->
                    // TODO: Fjern personId-lookupen her når denne ikke blir kalt fra gammel ForsorgerpliktRessurs
                    val updated = ansvar.find { it.familieKey == uuid } ?: ansvar.find { it.personId == existing.personId }
                    if (updated != null) {
                        uuid to existing.copy(deltBosted = updated.deltBosted)
                    } else uuid to existing
                }.toMap()
            )
            familieRepository.save(updated)
        } ?: error("Fant ingen familie å oppdatere")

    fun updateSivilstand(soknadId: UUID, sivilstatus: Sivilstatus?, ektefelle: Ektefelle?): Familie {
        return familieRepository.findById(soknadId).getOrNull()?.also {
            if (it.sivilstatus == Sivilstatus.GIFT && it.ektefelle?.kildeErSystem == true) {
                error("Kan ikke oppdatere ektefelle når ektefelle er innhentet fra folkeregisteret")
            }
        }?.let { familie ->
            val updated = familie.copy(
                sivilstatus = sivilstatus,
                ektefelle = ektefelle
            )
            familieRepository.save(updated)
        } ?: error("Fant ingen familie å oppdatere") // TODO ikke sikkert det finnes på dette tidspunktet
    }
}

private fun <A : Any, B> Map<A, B?>.filterNotNullValue(): Map<A, B> {
    return filter { it.value != null }.mapValues { it.value!! }
}
