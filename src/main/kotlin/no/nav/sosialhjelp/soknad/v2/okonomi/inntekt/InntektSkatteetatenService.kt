package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType.JOBB
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType.UTBETALING_SKATTEETATEN
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface InntektSkatteetatenService {
    fun getSamtykkeSkatt(soknadId: UUID): Bekreftelse?

    fun getInntektSkatt(soknadId: UUID): Inntekt?

    fun updateSamtykkeSkatt(
        soknadId: UUID,
        samtykke: Boolean,
    )

    fun saveUtbetalinger(
        soknadId: UUID,
        utbetalinger: List<Utbetaling>,
    )

    fun createJobbElement(soknadId: UUID)

    fun clearInntektSkatt(soknadId: UUID)
}

@Service
class InntektSkatteetatenServiceImpl(
    private val okonomiService: OkonomiService,
) : InntektSkatteetatenService {
    @Transactional(readOnly = true)
    override fun getSamtykkeSkatt(soknadId: UUID): Bekreftelse? =
        okonomiService.getBekreftelser(soknadId)
            .find { it.type == UTBETALING_SKATTEETATEN_SAMTYKKE }

    @Transactional(readOnly = true)
    override fun getInntektSkatt(soknadId: UUID): Inntekt? {
        return getSamtykkeSkatt(soknadId)?.let { samtykke ->
            if (samtykke.verdi) {
                okonomiService.getInntekter(soknadId).find { it.type == UTBETALING_SKATTEETATEN }
            } else {
                null
            }
        }
    }

    @Transactional
    override fun updateSamtykkeSkatt(
        soknadId: UUID,
        samtykke: Boolean,
    ) {
        // starter på scratch da vi regner med endring ved oppdatering
        okonomiService.removeElementFromOkonomi(soknadId, UTBETALING_SKATTEETATEN)
        okonomiService.removeElementFromOkonomi(soknadId, JOBB)

        okonomiService.updateBekreftelse(soknadId, UTBETALING_SKATTEETATEN_SAMTYKKE, samtykke)
    }

    @Transactional
    override fun saveUtbetalinger(
        soknadId: UUID,
        utbetalinger: List<Utbetaling>,
    ) {
        if (utbetalinger.isEmpty()) return

        Inntekt(
            type = UTBETALING_SKATTEETATEN,
            inntektDetaljer =
                OkonomiDetaljer(
                    detaljer = utbetalinger,
                ),
        )
            .also { okonomiService.addElementToOkonomi(soknadId, it) }
    }

    // hvis fetch fra skatt feiler, opprettes en dokumentasjonskrav så bruker kan laste opp selv
    @Transactional
    override fun createJobbElement(soknadId: UUID) {
        okonomiService.removeElementFromOkonomi(soknadId, UTBETALING_SKATTEETATEN)
        okonomiService.addElementToOkonomi(soknadId, type = JOBB)
    }

    override fun clearInntektSkatt(soknadId: UUID) {
        okonomiService.removeElementFromOkonomi(soknadId, UTBETALING_SKATTEETATEN)
    }
}
