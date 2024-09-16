package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import org.springframework.stereotype.Service
import java.util.UUID

interface BarneutgiftService {
    fun getBarneutgifter(soknadId: UUID): Set<Utgift>?

    fun removeBarneutgifter(soknadId: UUID)

    fun updateBarneutgifter(
        soknadId: UUID,
        existingBarneutgifter: Set<UtgiftType>,
    )
}

@Service
class BarneutgiftServiceImpl(
    private val okonomiService: OkonomiService,
) : BarneutgiftService {
    override fun getBarneutgifter(soknadId: UUID): Set<Utgift>? {
        return okonomiService.getBekreftelser(soknadId).find { it.type == BekreftelseType.BEKREFTELSE_BARNEUTGIFTER }
            ?.let {
                okonomiService.getUtgifter(soknadId).filter { barneutgiftTypes.contains(it.type) }.toSet()
            }
    }

    override fun removeBarneutgifter(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_BARNEUTGIFTER, verdi = false)
        barneutgiftTypes.forEach { okonomiService.removeElementFromOkonomi(soknadId, it) }
    }

    override fun updateBarneutgifter(
        soknadId: UUID,
        existingBarneutgifter: Set<UtgiftType>,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_BARNEUTGIFTER, verdi = true)
        barneutgiftTypes.forEach { type ->
            if (existingBarneutgifter.contains(type)) {
                okonomiService.addElementToOkonomi(soknadId, type)
            } else {
                okonomiService.removeElementFromOkonomi(soknadId, type)
            }
        }
    }

    companion object {
        private val barneutgiftTypes: Set<UtgiftType> =
            setOf(
                UtgiftType.UTGIFTER_BARNEHAGE,
                UtgiftType.UTGIFTER_SFO,
                UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER,
                UtgiftType.UTGIFTER_BARN_TANNREGULERING,
                UtgiftType.UTGIFTER_ANNET_BARN,
            )
    }
}
