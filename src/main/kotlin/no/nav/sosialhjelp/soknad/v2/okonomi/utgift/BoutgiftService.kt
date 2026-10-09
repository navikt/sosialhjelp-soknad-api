package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface BoutgiftService {
    fun getBoutgifter(soknadId: UUID): Set<Utgift>?

    fun removeBoutgifter(soknadId: UUID)

    fun updateBoutgifter(
        soknadId: UUID,
        existingBoutgifter: Set<UtgiftType>,
    )

    fun skalViseInfo(soknadId: UUID): Boolean
}

@Service
class BoutgiftServiceImpl(
    private val okonomiService: OkonomiService,
    private val integrasjonstatusRepository: IntegrasjonstatusRepository,
) : BoutgiftService {
    override fun getBoutgifter(soknadId: UUID): Set<Utgift>? {
        return okonomiService.getBekreftelser(soknadId).find { it.type == BekreftelseType.BEKREFTELSE_BOUTGIFTER }
            ?.let { bostotteBekreftelse ->
                if (!bostotteBekreftelse.verdi) {
                    emptySet()
                } else {
                    okonomiService.getUtgifter(soknadId)
                        .filter { boutgiftTypes.contains(it.type) }
                        .toSet()
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
            if (existingBoutgifter.contains(it)) {
                okonomiService.addElementToOkonomi(soknadId, it)
            } else {
                okonomiService.removeElementFromOkonomi(soknadId, it)
            }
        }
    }

    override fun skalViseInfo(soknadId: UUID): Boolean {
        return if (fetchBostotteFailed(soknadId)) {
            // hvis fetch av bostøtte feilet - ikke vis info (returner false)
            false
        } else {
            // hvis ingen bostøtte-utbetalinger eller saker - vis info (returner true)
            trueIfNoBostottesakerOrUtbetalinger(soknadId)
        }
    }

    private fun fetchBostotteFailed(soknadId: UUID): Boolean {
        return integrasjonstatusRepository.findByIdOrNull(soknadId)?.feilStotteHusbanken == true
    }

    private fun trueIfNoBostottesakerOrUtbetalinger(soknadId: UUID): Boolean {
        return okonomiService.getInntekter(soknadId).none { it.type == InntektType.UTBETALING_HUSBANKEN } &&
            okonomiService.getBostotteSaker(soknadId).isEmpty()
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
