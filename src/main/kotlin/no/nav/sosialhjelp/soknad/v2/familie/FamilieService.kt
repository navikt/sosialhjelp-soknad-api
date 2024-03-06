package no.nav.sosialhjelp.soknad.v2.familie

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
class FamilieService(private val familieRepository: FamilieRepository) {
    fun findFamilie(soknadId: UUID): Familie? {
        return familieRepository.findByIdOrNull(soknadId)
    }

    fun updateForsorger(soknadId: UUID, barnebidrag: Barnebidrag?, updated: List<Barn>): Familie {

        return (findFamilie(soknadId) ?: Familie(soknadId))
            .run {
                copy(
                    barnebidrag = barnebidrag,
                    ansvar = mapAnsvar(ansvar, updated)
                )
            }
            .let { familieRepository.save(it) }
    }

    private fun mapAnsvar(existing: Map<UUID, Barn>, updated: List<Barn>,): Map<UUID, Barn> {
        return existing
            .map { (uuid, existing) ->
                // TODO: Fjern personId-lookupen her når denne ikke blir kalt fra gammel ForsorgerpliktRessurs
                val updatedBarn = updated.find { it.familieKey == uuid } ?: updated.find { it.personId == existing.personId }

                when(updatedBarn != null) {
                    true -> uuid to existing.copy(deltBosted = updatedBarn.deltBosted)
                    false -> uuid to existing
                }
            }
            .toMap()
    }

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
