package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdService
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRegisterService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold as V2Arbeidsforhold

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
        } ?: logger.info("NyModell: Register: Kunne ikke hente arbeidsforhold, eller det finnes ikke for person")
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
