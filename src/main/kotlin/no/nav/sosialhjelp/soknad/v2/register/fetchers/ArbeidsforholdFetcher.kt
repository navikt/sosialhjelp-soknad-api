package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.arbeid.AaregService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRegisterService
import no.nav.sosialhjelp.soknad.v2.register.AsynchronousFetcher
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ArbeidsforholdFetcher(
    private val arbeidsforholdService: AaregService,
    private val livssituasjonService: LivssituasjonRegisterService,
) : AsynchronousFetcher {
    private val logger by logger()

    override suspend fun fetchAndSave(soknadId: UUID) {
        logger.info("Henter arbeidsforhold fra Aa-registeret")

        val ctx = currentUserContext()
        arbeidsforholdService.hentArbeidsforhold(ctx.token, ctx.userId)
            ?.let { arbeidsforhold ->
                livssituasjonService.updateArbeidsforhold(
                    soknadId,
                    arbeidsforhold = arbeidsforhold,
                )
            }
            ?: logger.info("Kunne ikke hente arbeidsforhold fra register, eller det finnes ikke for person")
    }
}
