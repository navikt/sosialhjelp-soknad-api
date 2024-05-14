package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdService
import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.service.LivssituasjonRegisterService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ArbeidsforholdFetcher(
    private val arbeidsforholdService: ArbeidsforholdService,
    private val livssituasjonService: LivssituasjonRegisterService,
) : RegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
        logger.info("NyModell: Register: Henter arbeidsforhold fra Aa-registeret")

        arbeidsforholdService.hentArbeidsforhold(getUserIdFromToken())?.let { arbeidsforholdList ->
            livssituasjonService.updateArbeidsforhold(
                soknadId = soknadId,
                arbeidsforhold = arbeidsforholdList.map { it.toV2Arbeidsforhold() },
            )
            // TODO Aareg-klienten returnerer null for mange exceptions - vanskelig å tolke null her
        } ?: logger.info("NyModell: Register: Kunne ikke hente arbeidsforhold, eller det finnes ikke for person")

        // TODO Vedleggsforventninger?
    }
}
