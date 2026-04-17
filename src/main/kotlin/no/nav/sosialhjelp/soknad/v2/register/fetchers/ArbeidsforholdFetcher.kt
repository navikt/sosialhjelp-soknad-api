package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.arbeid.AaregService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRegisterService
import no.nav.sosialhjelp.soknad.v2.register.AsynchronousFetcher
import no.nav.sosialhjelp.soknad.v2.register.UserContext
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ArbeidsforholdFetcher(
    private val arbeidsforholdService: AaregService,
    private val livssituasjonService: LivssituasjonRegisterService,
) : AsynchronousFetcher {
    private val logger by logger()

    override fun fetchAndSave(
        soknadId: UUID,
        userContext: UserContext,
    ) {
        logger.info("Henter arbeidsforhold fra Aa-registeret")

        arbeidsforholdService.hentArbeidsforhold(userContext)
            ?.let { arbeidsforhold ->
                livssituasjonService.updateArbeidsforhold(
                    soknadId,
                    arbeidsforhold = arbeidsforhold,
                )
            }
            ?: logger.info("Kunne ikke hente arbeidsforhold fra register, eller det finnes ikke for person")
    }
}
