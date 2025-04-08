package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
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

interface NavYtelseService {
    fun getNavYtelse(soknadId: UUID): Inntekt?

    fun getIntegrasjonStatusNavYtelse(soknadId: UUID): Boolean?
}

@Service
class InntektService(
    private val okonomiService: OkonomiService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) : StudielanService, UtbetalingerService, NavYtelseService {
    @Transactional(readOnly = true)
    override fun getHarStudielan(soknadId: UUID): Boolean? {
        return okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.STUDIELAN_BEKREFTELSE }
            ?.verdi
    }

    @Transactional
    override fun updateStudielan(
        soknadId: UUID,
        mottarStudielan: Boolean,
    ) {
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

    @Transactional(readOnly = true)
    override fun getUtbetalinger(soknadId: UUID): List<Inntekt>? {
        return okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.BEKREFTELSE_UTBETALING }
            ?.verdi
            ?.let { bekreftelse ->
                if (bekreftelse) {
                    okonomiService.getInntekter(soknadId)
                        .filter { brukerUtbetalingerTypes.contains(it.type) }
                } else {
                    emptyList()
                }
            }
    }

    @Transactional
    override fun updateUtbetalinger(
        soknadId: UUID,
        eksisterendeTyper: Set<InntektType>,
        beskrivelseAnnet: String?,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_UTBETALING, verdi = true)

        brukerUtbetalingerTypes.forEach { type ->
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

    @Transactional
    override fun removeUtbetalinger(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_UTBETALING, verdi = false)

        brukerUtbetalingerTypes.forEach {
            okonomiService.removeElementFromOkonomi(soknadId, it)
        }
    }

    override fun getNavYtelse(soknadId: UUID): Inntekt? {
        return okonomiService.getInntekter(soknadId).find { it.type == InntektType.UTBETALING_NAVYTELSE }
    }

    override fun getIntegrasjonStatusNavYtelse(soknadId: UUID): Boolean? {
        return integrasjonStatusService.hasFetchUtbetalingerFraNavFailed(soknadId)
    }

    companion object {
        private val brukerUtbetalingerTypes: List<InntektType> =
            listOf(
                InntektType.UTBETALING_UTBYTTE,
                InntektType.UTBETALING_SALG,
                InntektType.UTBETALING_FORSIKRING,
                InntektType.UTBETALING_ANNET,
            )
    }
}
