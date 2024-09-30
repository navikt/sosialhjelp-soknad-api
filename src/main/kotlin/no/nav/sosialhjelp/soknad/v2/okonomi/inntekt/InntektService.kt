package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType.UTBETALING_SKATTEETATEN
import no.nav.sosialhjelp.soknad.v2.register.fetchers.InntektSkatteetatenFetcher
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

interface InntektSkatteetatenService {
    fun getSamtykkeSkatt(soknadId: UUID): Bekreftelse?

    fun getInntektSkatt(soknadId: UUID): Inntekt?

    fun getIntegrasjonStatusSkatt(soknadId: UUID): Boolean?

    fun updateSamtykkeSkatt(
        soknadId: UUID,
        samtykke: Boolean,
    )
}

@Service
@Transactional
class InntektService(
    private val okonomiService: OkonomiService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val inntektSkatteetatenFetcher: InntektSkatteetatenFetcher,
) : StudielanService, UtbetalingerService, InntektSkatteetatenService {
    override fun getHarStudielan(soknadId: UUID): Boolean? {
        return okonomiService.getBekreftelser(soknadId)
            .find { it.type == BekreftelseType.STUDIELAN_BEKREFTELSE }
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

    override fun removeUtbetalinger(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_UTBETALING, verdi = false)

        brukerUtbetalingerTypes.forEach {
            okonomiService.removeElementFromOkonomi(soknadId, it)
        }
    }

    override fun getSamtykkeSkatt(soknadId: UUID): Bekreftelse? =
        okonomiService.getBekreftelser(soknadId)
            .find { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE }

    override fun getInntektSkatt(soknadId: UUID): Inntekt? {
        return getSamtykkeSkatt(soknadId)?.let { samtykke ->
            if (samtykke.verdi) {
                okonomiService.getInntekter(soknadId).find { it.type == UTBETALING_SKATTEETATEN }
            } else {
                null
            }
        }
    }

    override fun getIntegrasjonStatusSkatt(soknadId: UUID): Boolean? {
        return integrasjonStatusService.getInntektSkatteetatenStatus(soknadId)
    }

    override fun updateSamtykkeSkatt(
        soknadId: UUID,
        samtykke: Boolean,
    ) {
        if (samtykke == getSamtykkeSkatt(soknadId)?.verdi) return

        okonomiService.updateBekreftelse(soknadId, UTBETALING_SKATTEETATEN_SAMTYKKE, samtykke)

        if (samtykke) {
            inntektSkatteetatenFetcher.fetchAndSave(soknadId)
        } else {
            okonomiService.removeElementFromOkonomi(soknadId, UTBETALING_SKATTEETATEN)
        }
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
