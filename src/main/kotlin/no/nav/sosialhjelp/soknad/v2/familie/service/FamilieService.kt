package no.nav.sosialhjelp.soknad.v2.familie.service

import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class FamilieService(
    private val familieRepository: FamilieRepository
): ForsorgerService, SivilstandService, FamilieRegisterService {
    override fun findForsorger(soknadId: UUID) = familieRepository.findByIdOrNull(soknadId)?.toForsorger()

    override fun updateForsorger(
        soknadId: UUID,
        barnebidrag: Barnebidrag?,
        updated: List<Barn>,
    ): Forsorger {
        return familieRepository
            .findOrCreate(soknadId)
            .run {
                copy(
                    barnebidrag = barnebidrag,
                    ansvar = mapAnsvar(ansvar, updated),
                )
            }
            .let { familieRepository.save(it) }
            .toForsorger()
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

    override fun findSivilstand(soknadId: UUID) = familieRepository.findByIdOrNull(soknadId)?.toSivilstand()

    override fun updateSivilstand(
        soknadId: UUID,
        sivilstatus: Sivilstatus?,
        ektefelle: Ektefelle?,
    ): Sivilstand {
        return familieRepository
            .findOrCreate(soknadId)
            .also {
                if (it.ektefelle?.kildeErSystem == true) {
                    error("Kan ikke oppdatere ektefelle når ektefelle er innhentet fra folkeregisteret")
                }
            }
            .copy(
                sivilstatus = sivilstatus,
                ektefelle = ektefelle
            )
            .let { familieRepository.save(it) }
            .toSivilstand()
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
            .run {
                copy(
                    harForsorgerplikt = harForsorgerplikt,
                    ansvar = barn.associateBy { UUID.randomUUID() }
                )
            }
            .also { familieRepository.save(it) }
    }
}

internal fun FamilieRepository.findOrCreate(soknadId: UUID): Familie {
    return findByIdOrNull(soknadId) ?: Familie(soknadId)
}
