package no.nav.sosialhjelp.soknad.v2.familie.service

import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface ForsorgerService {
    fun findForsorger(soknadId: UUID): Forsorger?

    fun updateForsorger(
        soknadId: UUID,
        barnebidrag: Barnebidrag?,
        updated: List<Barn>,
    ): Forsorger
}

interface SivilstandService {
    fun findSivilstand(soknadId: UUID): Sivilstand?

    fun updateSivilstand(
        soknadId: UUID,
        sivilstatus: Sivilstatus?,
        ektefelle: Ektefelle?,
    ): Sivilstand
}

@Service
class FamilieServiceImpl(
    private val familieRepository: FamilieRepository,
    private val okonomiService: OkonomiService,
) : ForsorgerService, SivilstandService {
    override fun findForsorger(soknadId: UUID) = familieRepository.findByIdOrNull(soknadId)?.toForsorger()

    override fun updateForsorger(
        soknadId: UUID,
        barnebidrag: Barnebidrag?,
        updated: List<Barn>,
    ): Forsorger {
        val forsorger =
            findOrCreate(soknadId)
                .run {
                    copy(
                        barnebidrag = barnebidrag,
                        ansvar = mapAnsvar(ansvar, updated),
                    )
                }
                .let { familieRepository.save(it) }
                .toForsorger()

        // TODO Trenger denne å være en del av familie-modellen i det hele tatt? Lever på siden uavhengig av om vedkommende...
        // TODO ...har forsorgerplikt eller ei
        handleBarnebidrag(soknadId, barnebidrag)

        return forsorger
    }

    private fun handleBarnebidrag(
        soknadId: UUID,
        barnebidrag: Barnebidrag?,
    ) {
        when (barnebidrag) {
            Barnebidrag.BEGGE -> updateBarnebidrag(soknadId, inntektPresent = true, utgiftPresent = true)
            Barnebidrag.BETALER -> updateBarnebidrag(soknadId, inntektPresent = false, utgiftPresent = true)
            Barnebidrag.MOTTAR -> updateBarnebidrag(soknadId, inntektPresent = true, utgiftPresent = false)
            else -> updateBarnebidrag(soknadId, inntektPresent = false, utgiftPresent = false)
        }
    }

    private fun updateBarnebidrag(
        soknadId: UUID,
        inntektPresent: Boolean,
        utgiftPresent: Boolean,
    ) {
        okonomiService.updateInntekt(soknadId, InntektType.BARNEBIDRAG_MOTTAR, inntektPresent)
        okonomiService.updateUtgift(soknadId, UtgiftType.BARNEBIDRAG_BETALER, utgiftPresent)
    }

    private fun mapAnsvar(
        existing: Map<UUID, Barn>,
        updated: List<Barn>,
    ): Map<UUID, Barn> {
        return existing
            .map { (uuid, existing) ->
                // TODO: Fjern personId-lookupen her når denne ikke blir kalt fra gammel ForsorgerpliktRessurs
                val updatedBarn =
                    updated.find { it.familieKey == uuid }
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
        return findOrCreate(soknadId)
            .also {
                if (it.ektefelle?.kildeErSystem == true) {
                    error("Kan ikke oppdatere ektefelle når ektefelle er innhentet fra folkeregisteret")
                }
            }
            .copy(
                sivilstatus = sivilstatus,
                ektefelle = ektefelle,
            )
            .let { familieRepository.save(it) }
            .toSivilstand()
    }

    private fun findOrCreate(soknadId: UUID): Familie {
        return familieRepository.findByIdOrNull(soknadId)
            ?: familieRepository.save(Familie(soknadId))
    }
}

// wrapper-klasse (midlertidig sålenge vi har et såpass fragmentert controller-lag?)
data class Forsorger(
    val harForsorgerplikt: Boolean? = null,
    val barnebidrag: Barnebidrag? = null,
    val ansvar: Map<UUID, Barn> = emptyMap(),
)

fun Familie.toForsorger() =
    Forsorger(
        harForsorgerplikt = harForsorgerplikt,
        barnebidrag = barnebidrag,
        ansvar = ansvar,
    )

// wrapper-klasse (midlertidig sålenge vi har et såpass fragmentert controller-lag?)
data class Sivilstand(
    val sivilstatus: Sivilstatus? = null,
    val ektefelle: Ektefelle? = null,
)

internal fun Familie.toSivilstand() =
    Sivilstand(
        sivilstatus = sivilstatus,
        ektefelle = ektefelle,
    )
