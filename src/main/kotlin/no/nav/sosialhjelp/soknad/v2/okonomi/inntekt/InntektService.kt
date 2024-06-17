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
        input: HarUtbetalingerInput,
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
        input: HarUtbetalingerInput,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_UTBETALING, verdi = true)

        updateUtbetaling(soknadId, InntektType.UTBETALING_UTBYTTE, input.hasUtbytte)
        updateUtbetaling(soknadId, InntektType.UTBETALING_SALG, input.hasSalg)
        updateUtbetaling(soknadId, InntektType.UTBETALING_FORSIKRING, input.hasForsikring)
        updateUtbetaling(
            soknadId = soknadId,
            type = InntektType.UTBETALING_ANNET,
            hasUtbetaling = input.hasAnnet,
            beskrivelse = if (input.hasAnnet) input.beskrivelseUtbetaling else null,
        )
    }

    override fun removeUtbetalinger(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_UTBETALING, verdi = false)

        utbetalingerTypes.forEach {
            okonomiService.removeElementFromOkonomi(soknadId, it)
        }
    }

    private fun updateUtbetaling(
        soknadId: UUID,
        type: InntektType,
        hasUtbetaling: Boolean,
        beskrivelse: String? = null,
    ) {
        if (hasUtbetaling) {
            okonomiService.addElementToOkonomi(soknadId, type, beskrivelse)
        } else {
            okonomiService.removeElementFromOkonomi(soknadId, type)
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
