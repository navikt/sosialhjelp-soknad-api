package no.nav.sosialhjelp.soknad.v2.familie

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

interface ForsorgerService {
    fun findForsorger(soknadId: UUID): Forsorger?
    fun updateForsorger(soknadId: UUID, barnebidrag: Barnebidrag?, updated: List<Barn>): Forsorger
}

interface SivilstandService {
    fun findSivilstand(soknadId: UUID): Sivilstand?
    fun updateSivilstand(soknadId: UUID, sivilstatus: Sivilstatus?, ektefelle: Ektefelle?): Sivilstand
}

interface FamilieRegisterService {
    fun updateSivilstatusFraRegister(soknadId: UUID, sivilstatus: Sivilstatus, ektefelle: Ektefelle)
    fun updateForsorgerpliktRegister(soknadId: UUID, harForsorgerplikt: Boolean, barn: List<Barn>)
}

@Component
class FamilieServiceImpl(
    private val familieRepository: FamilieRepository
): ForsorgerService, SivilstandService, FamilieRegisterService {
    override fun findForsorger(soknadId: UUID) = familieRepository.findByIdOrNull(soknadId)?.forsorger

    override fun updateForsorger(
        soknadId: UUID,
        barnebidrag: Barnebidrag?,
        updated: List<Barn>,
    ): Forsorger {
        return familieRepository
            .findOrCreate(soknadId)
            .run {
                copy(
                    forsorger = forsorger.copy(
                        barnebidrag = barnebidrag,
                        ansvar = mapAnsvar(forsorger.ansvar, updated),
                    )
                )
            }
            .let { familieRepository.save(it) }
            .forsorger
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

    override fun findSivilstand(soknadId: UUID) = familieRepository.findByIdOrNull(soknadId)?.sivilstand

    override fun updateSivilstand(
        soknadId: UUID,
        sivilstatus: Sivilstatus?,
        ektefelle: Ektefelle?,
    ): Sivilstand {
        return familieRepository
            .findOrCreate(soknadId)
            .also {
                if (it.sivilstand.ektefelle?.kildeErSystem == true) {
                    error("Kan ikke oppdatere ektefelle når ektefelle er innhentet fra folkeregisteret")
                }
            }
            .run {
                copy(
                    sivilstand = sivilstand.copy(
                        sivilstatus = sivilstatus,
                        ektefelle = ektefelle
                    ),
                )
            }
            .let { familieRepository.save(it) }
            .sivilstand
    }

    override fun updateSivilstatusFraRegister(
        soknadId: UUID,
        sivilstatus: Sivilstatus,
        ektefelle: Ektefelle
    ) {
        familieRepository
            .findOrCreate(soknadId)
            .run {
                copy(
                    sivilstand = sivilstand.copy(
                        sivilstatus = sivilstatus,
                        ektefelle = ektefelle
                    )
                )
            }
            .also { familieRepository.save(it) }
    }

    override fun updateForsorgerpliktRegister(
        soknadId: UUID,
        harForsorgerplikt: Boolean,
        barn: List<Barn>
    ) {
        familieRepository
            .findOrCreate(soknadId)
            .run {
                copy(
                    forsorger = forsorger.copy(
                        harForsorgerplikt = harForsorgerplikt,
                        ansvar = barn.associateBy { UUID.randomUUID() }
                    )
                )
            }
            .also { familieRepository.save(it) }
    }
}

private fun FamilieRepository.findOrCreate(soknadId: UUID): Familie {
    return findByIdOrNull(soknadId) ?: Familie(soknadId)
}