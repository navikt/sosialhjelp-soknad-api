package no.nav.sosialhjelp.soknad.v2.register.handlers

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdService
import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataHandler
import org.springframework.stereotype.Component

@Component
class HandleArbeidsforhold(
    private val arbeidsforholdService: ArbeidsforholdService,
    private val livssituasjonService: LivssituasjonService,
): RegisterDataHandler {
    private val log by logger()

    override fun handle(soknadId: UUID) {

        arbeidsforholdService.hentArbeidsforhold(getUserIdFromToken())?.let { arbeidsforholdList ->
            livssituasjonService.updateArbeidsforhold(
                soknadId = soknadId,
                arbeidsforhold = arbeidsforholdList.map { it.toV2Arbeidsforhold() }
            )
            // TODO Aareg-klienten returnerer null for mange exceptions - vanskelig å tolke null her
        } ?: log.info("Kunne ikke hente arbeidsforhold, eller det finnes ikke for person")

        // TODO Vedleggsforventninger?
    }
}