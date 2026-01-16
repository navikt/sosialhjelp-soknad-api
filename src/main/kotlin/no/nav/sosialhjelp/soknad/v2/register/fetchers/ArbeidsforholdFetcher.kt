package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.arbeid.AaregService
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRegisterService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold as V2Arbeidsforhold

@Component
class ArbeidsforholdFetcher(
    private val arbeidsforholdService: AaregService,
    private val livssituasjonService: LivssituasjonRegisterService,
) : RegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
        logger.info("Henter arbeidsforhold fra Aa-registeret")

        arbeidsforholdService.hentArbeidsforhold()?.let { arbeidsforholdList ->
            livssituasjonService.updateArbeidsforhold(
                soknadId = soknadId,
                arbeidsforhold = arbeidsforholdList.map { it.toV2Arbeidsforhold() },
            )
        } ?: logger.info("Kunne ikke hente arbeidsforhold fra register, eller det finnes ikke for person")
    }
}

private fun Arbeidsforhold.toV2Arbeidsforhold(): V2Arbeidsforhold {
    return V2Arbeidsforhold(
        orgnummer = this.orgnr,
        arbeidsgivernavn = this.arbeidsgivernavn,
        start = this.fom,
        slutt = this.tom,
        fastStillingsprosent = this.fastStillingsprosent?.toInt(),
        harFastStilling = this.harFastStilling,
    )
}
