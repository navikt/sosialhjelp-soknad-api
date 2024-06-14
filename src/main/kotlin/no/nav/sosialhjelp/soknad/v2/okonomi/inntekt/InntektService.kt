package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface StudielanService {
    fun getHarStudielan(soknadId: UUID): Boolean?

    fun updateStudielan(
        soknadId: UUID,
        mottarStudielan: Boolean,
    )
}

@Service
@Transactional
class InntektService(
    private val okonomiService: OkonomiService,
) : StudielanService {
    override fun getHarStudielan(soknadId: UUID): Boolean? {
        return okonomiService.getBekreftelser(soknadId)
            ?.find { it.type == BekreftelseType.STUDIELAN_BEKREFTELSE }
            ?.verdi
    }

    override fun updateStudielan(
        soknadId: UUID,
        mottarStudielan: Boolean,
    ) {
        // TODO bruker har huket av for "erStudent", huket av for "harStudielån", men så fjerner "erStudent" - hva da?
        okonomiService.updateBekreftelse(
            soknadId = soknadId,
            type = BekreftelseType.STUDIELAN_BEKREFTELSE,
            verdi = mottarStudielan,
        )

        if (mottarStudielan) {
            okonomiService.addElementToOkonomi(soknadId, InntektType.STUDIELAN_INNTEKT)
        } else {
            okonomiService.removeElementFromOkonomi(soknadId, InntektType.STUDIELAN_INNTEKT)
        }
    }
}
