package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.arbeid.dto.toDomain
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
open class ArbeidsforholdService(
    private val arbeidsforholdClient: ArbeidsforholdClient,
    private val organisasjonService: OrganisasjonService
) {

    open fun hentArbeidsforhold(fnr: String): List<Arbeidsforhold>? {
        val arbeidsforholdList = arbeidsforholdClient.finnArbeidsforholdForArbeidstaker(fnr)
        return arbeidsforholdList
            ?.map { it.toDomain(organisasjonService) }
            .also { log.info("Hentet ${it?.size ?: 0} arbeidsforhold fra aareg") }
    }

    companion object {
        private val log = getLogger(ArbeidsforholdService::class.java)
    }
}
