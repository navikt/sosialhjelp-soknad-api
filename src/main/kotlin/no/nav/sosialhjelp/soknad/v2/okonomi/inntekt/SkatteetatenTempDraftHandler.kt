package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.UserContextElement
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.register.fetchers.InntektSkatteetatenFetcher
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

// TODO: Fjern etter to uker
@Component
class SkatteetatenTempDraftHandler(
    private val okonomiService: OkonomiService,
    private val skatteetatenFetcher: InntektSkatteetatenFetcher,
) {
    fun fetchData(soknadId: UUID) {
        if (okonomiService.getBekreftelser(soknadId).any { it.type == BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE }) return
        if (okonomiService.getInntekter(soknadId).any { it.type == InntektType.UTBETALING_SKATTEETATEN }) return

        runBlocking(Dispatchers.IO + MDCContext(MDC.getCopyOfContextMap()) + UserContextElement(getToken(), getUserIdFromToken())) {
            skatteetatenFetcher.fetchAndSave(soknadId)
        }
    }
}
