package no.nav.sosialhjelp.soknad.arbeid

import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.arbeid.dto.toDomain
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
open class ArbeidsforholdService(
    private val aaregClient: AaregClient,
    private val arbeidsforholdClient: ArbeidsforholdClient,
    private val organisasjonService: OrganisasjonService,
    private val unleash: Unleash
) {

    open fun hentArbeidsforhold(fnr: String): List<Arbeidsforhold>? {
        val arbeidsforholdList = if (unleash.isEnabled(AAREG_UTEN_FSS_PROXY, false)) {
            aaregClient.finnArbeidsforholdForArbeidstaker(fnr)
        } else {
            arbeidsforholdClient.finnArbeidsforholdForArbeidstaker(fnr)
        }
        return arbeidsforholdList
            ?.map { it.toDomain(organisasjonService) }
            .also { log.info("Hentet ${it?.size ?: 0} arbeidsforhold fra aareg") }
    }

    companion object {
        private val log = getLogger(ArbeidsforholdService::class.java)
        private const val AAREG_UTEN_FSS_PROXY = "sosialhjelp.soknad.aareg-uten-fss-proxy"
    }
}
