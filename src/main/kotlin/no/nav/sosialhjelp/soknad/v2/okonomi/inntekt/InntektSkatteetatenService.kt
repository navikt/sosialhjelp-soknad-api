package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.UserContextElement
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType.JOBB
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType.UTBETALING_SKATTEETATEN
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.register.fetchers.InntektSkatteetatenFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.slf4j.MDC
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface InntektSkatteetatenService {
    fun getInntektSkatt(soknadId: UUID): Inntekt?

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
    @param:Lazy
    private val skatteetatenFetcher: InntektSkatteetatenFetcher,
    private val integrasjonStatusService: IntegrasjonStatusService,
) : InntektSkatteetatenService {
    @Transactional(readOnly = true)
    override fun getInntektSkatt(soknadId: UUID): Inntekt? {
        val inntekt = okonomiService.getInntekter(soknadId).find { it.type == UTBETALING_SKATTEETATEN }
        // TODO: Fjern to uker etter fjerning av samtykke
        if (inntekt == null && integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknadId) != false) {
            runBlocking(Dispatchers.IO + MDCContext(MDC.getCopyOfContextMap()) + UserContextElement(getToken(), getUserIdFromToken())) {
                skatteetatenFetcher.fetchAndSave(soknadId)
            }
            return okonomiService.getInntekter(soknadId).find { it.type == UTBETALING_SKATTEETATEN }
        }
        return inntekt
    }

    @Transactional
    override fun saveUtbetalinger(
        soknadId: UUID,
        utbetalinger: List<Utbetaling>,
    ) {
        if (utbetalinger.isNotEmpty()) {
            Inntekt(
                type = UTBETALING_SKATTEETATEN,
                inntektDetaljer =
                    OkonomiDetaljer(
                        detaljer = utbetalinger,
                    ),
            )
                .also { okonomiService.addElementToOkonomi(soknadId, it) }
        }
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
