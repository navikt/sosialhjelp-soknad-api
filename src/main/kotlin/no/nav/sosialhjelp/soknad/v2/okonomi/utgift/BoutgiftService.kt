package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import org.springframework.stereotype.Service
import java.util.UUID

interface BoutgiftService {
    fun getBoutgifter(soknadId: UUID): Set<Utgift>?

    fun removeBoutgifter(soknadId: UUID)

    fun updateBoutgifter(
        soknadId: UUID,
        existingBoutgifter: Set<UtgiftType>,
    )

    fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean
}

@Service
class BoutgiftServiceImpl(private val okonomiService: OkonomiService) : BoutgiftService {
    override fun getBoutgifter(soknadId: UUID): Set<Utgift>? {
        return okonomiService.getBekreftelser(soknadId).find { it.type == BekreftelseType.BEKREFTELSE_BOUTGIFTER }
            ?.let { bostotteBekreftelse ->
                when (bostotteBekreftelse.verdi) {
                    false -> emptySet()
                    else -> okonomiService.getUtgifter(soknadId).filter { boutgiftTypes.contains(it.type) }.toSet()
                }
            }
    }

    override fun removeBoutgifter(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = false)
        boutgiftTypes.forEach { okonomiService.removeElementFromOkonomi(soknadId, it) }
    }

    override fun updateBoutgifter(
        soknadId: UUID,
        existingBoutgifter: Set<UtgiftType>,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)

        boutgiftTypes.forEach {
            when (existingBoutgifter.contains(it)) {
                true -> okonomiService.addElementToOkonomi(soknadId, it)
                false -> okonomiService.removeElementFromOkonomi(soknadId, it)
            }
        }
    }

    override fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean {
        return when (hasBoutgifter(soknadId)) {
            false -> false
            true -> hasNoBostotte(soknadId)
        }
    }

    private fun hasBoutgifter(soknadId: UUID): Boolean =
        okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.BEKREFTELSE_BOUTGIFTER }?.verdi == true

    private fun hasNoBostotte(soknadId: UUID): Boolean {
        return okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.BOSTOTTE }
            .let { it?.verdi != true }
    }

    companion object {
        private val boutgiftTypes: Set<UtgiftType> =
            setOf(
                UtgiftType.UTGIFTER_HUSLEIE,
                UtgiftType.UTGIFTER_STROM,
                UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT,
                UtgiftType.UTGIFTER_OPPVARMING,
                UtgiftType.UTGIFTER_BOLIGLAN,
                UtgiftType.UTGIFTER_ANNET_BO,
            )
    }
}
