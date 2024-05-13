package no.nav.sosialhjelp.soknad.v2.familie.service

import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

// TODO Denne kjører med Prop.NESTED fordi den ikke må ødelegge for annen skriving
@Transactional(propagation = Propagation.NESTED)
@Service
class FamilieRegisterService(private val familieRepository: FamilieRepository) {
    fun updateSivilstatusFromRegister(
        soknadId: UUID,
        sivilstatus: Sivilstatus,
        ektefelle: Ektefelle,
    ) {
        findOrCreate(soknadId)
            .copy(
                sivilstatus = sivilstatus,
                ektefelle = ektefelle,
            )
            .also { familieRepository.save(it) }
    }

    fun updateForsorgerpliktRegister(
        soknadId: UUID,
        harForsorgerplikt: Boolean,
        barn: List<Barn>,
    ) {
        findOrCreate(soknadId)
            .run {
                copy(
                    harForsorgerplikt = harForsorgerplikt,
                    ansvar = barn.associateBy { UUID.randomUUID() },
                )
            }
            .also { familieRepository.save(it) }
    }

    private fun findOrCreate(soknadId: UUID): Familie {
        return familieRepository.findByIdOrNull(soknadId)
            ?: familieRepository.save(Familie(soknadId))
    }
}
