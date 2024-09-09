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

interface UtbetalingerService {
    fun getUtbetalinger(soknadId: UUID): List<Inntekt>?

    fun updateUtbetalinger(
        soknadId: UUID,
        eksisterendeTyper: Set<InntektType>,
        beskrivelseAnnet: String?,
    )

    fun removeUtbetalinger(soknadId: UUID)
}

@Service
@Transactional
class InntektService(
    private val okonomiService: OkonomiService,
) : StudielanService, UtbetalingerService {
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
        // Svar -> Fjern alt
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

    override fun getUtbetalinger(soknadId: UUID): List<Inntekt>? {
        return okonomiService.getBekreftelser(soknadId)
            ?.find { it.type == BekreftelseType.BEKREFTELSE_UTBETALING }?.verdi
            ?.let { bekreftelse ->
                if (bekreftelse) {
                    okonomiService.getInntekter(soknadId)
                        ?.filter { utbetalingerTypes.contains(it.type) }
                } else {
                    emptyList()
                }
            }
    }

    override fun updateUtbetalinger(
        soknadId: UUID,
        eksisterendeTyper: Set<InntektType>,
        beskrivelseAnnet: String?,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_UTBETALING, verdi = true)

        utbetalingerTypes.forEach { type ->
            if (eksisterendeTyper.contains(type)) {
                okonomiService.addElementToOkonomi(
                    soknadId = soknadId,
                    type = type,
                    beskrivelse = if (type == InntektType.UTBETALING_ANNET) beskrivelseAnnet else null,
                )
            } else {
                okonomiService.removeElementFromOkonomi(soknadId, type)
            }
        }
    }

    override fun removeUtbetalinger(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_UTBETALING, verdi = false)

        utbetalingerTypes.forEach {
            okonomiService.removeElementFromOkonomi(soknadId, it)
        }
    }

    companion object {
        private val utbetalingerTypes: List<InntektType> =
            listOf(
                InntektType.UTBETALING_UTBYTTE,
                InntektType.UTBETALING_SALG,
                InntektType.UTBETALING_FORSIKRING,
                InntektType.UTBETALING_ANNET,
            )
    }
}
