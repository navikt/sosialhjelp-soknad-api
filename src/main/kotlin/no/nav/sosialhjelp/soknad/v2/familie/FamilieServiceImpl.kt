package no.nav.sosialhjelp.soknad.v2.familie

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

interface FamilieService {
    fun findFamilie(soknadId: UUID): Familie?
    fun updateForsorger(soknadId: UUID, barnebidrag: Barnebidrag?, updated: List<Barn>): Familie
    fun updateSivilstand(soknadId: UUID, sivilstatus: Sivilstatus?, ektefelle: Ektefelle?): Familie
}

interface FamilieRegisterService {
    fun updateSivilstatusFraRegister(soknadId: UUID, sivilstatus: Sivilstatus, ektefelle: Ektefelle)
    fun updateForsorgerpliktRegister(soknadId: UUID, harForsorgerplikt: Boolean, barn: List<Barn>)
}

@Component
class FamilieServiceImpl(
    private val familieRepository: FamilieRepository
): FamilieService, FamilieRegisterService {
    override fun findFamilie(soknadId: UUID) = familieRepository.findByIdOrNull(soknadId)

    override fun updateForsorger(
        soknadId: UUID,
        barnebidrag: Barnebidrag?,
        updated: List<Barn>,
    ): Familie {
        return familieRepository
            .findOrCreate(soknadId)
            .run {
                copy(
                    barnebidrag = barnebidrag,
                    ansvar = mapAnsvar(ansvar, updated),
                )
            }
            .let { familieRepository.save(it) }
    }

    private fun mapAnsvar(
        existing: Map<UUID, Barn>,
        updated: List<Barn>,
    ): Map<UUID, Barn> {
        return existing
            .map { (uuid, existing) ->
                // TODO: Fjern personId-lookupen her når denne ikke blir kalt fra gammel ForsorgerpliktRessurs
                val updatedBarn = updated.find { it.familieKey == uuid }
                    ?: updated.find { it.personId == existing.personId }

                when (updatedBarn != null) {
                    true -> uuid to existing.copy(deltBosted = updatedBarn.deltBosted)
                    false -> uuid to existing
                }
            }
            .toMap()
    }

    override fun updateSivilstand(
        soknadId: UUID,
        sivilstatus: Sivilstatus?,
        ektefelle: Ektefelle?,
    ): Familie {
        return familieRepository
            .findOrCreate(soknadId)
            .also {
                // TODO Vil ikke kildeErSystem == true være nok til å kunne avgjøre dette?
                if (it.sivilstatus == Sivilstatus.GIFT && it.ektefelle?.kildeErSystem == true) {
                    error("Kan ikke oppdatere ektefelle når ektefelle er innhentet fra folkeregisteret")
                }
            }
            .copy(
                sivilstatus = sivilstatus,
                ektefelle = ektefelle
            )
            .also { familieRepository.save(it) }
    }

    override fun updateSivilstatusFraRegister(
        soknadId: UUID,
        sivilstatus: Sivilstatus,
        ektefelle: Ektefelle
    ) {
        familieRepository
            .findOrCreate(soknadId)
            .copy(
                sivilstatus = sivilstatus,
                ektefelle = ektefelle
            )
            .also { familieRepository.save(it) }
    }

    override fun updateForsorgerpliktRegister(
        soknadId: UUID,
        harForsorgerplikt: Boolean,
        barn: List<Barn>
    ) {
        familieRepository
            .findOrCreate(soknadId)
            .copy(
                harForsorgerplikt = harForsorgerplikt,
                ansvar = barn.associateBy { UUID.randomUUID() }
            )
            .also { familieRepository.save(it) }
    }
}

private fun FamilieRepository.findOrCreate(soknadId: UUID): Familie {
    return findByIdOrNull(soknadId) ?: Familie(soknadId)
}