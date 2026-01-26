package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
class AaregService(
    private val organisasjonService: OrganisasjonService,
    private val aaregClientV2: AaregClientV2,
) {
    fun hentArbeidsforholdV2(): List<no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold>? {
        val arbeidsforholdCreator = ArbeidsforholdCreator(organisasjonService)

        logger.info("Henter arbeidsforhold for bruker fra aareg-api v2")

        return runCatching {
            aaregClientV2.finnArbeidsforholdForArbeidstaker()
                ?.let { dto -> dto.map { arbeidsforholdCreator.createArbeidsforhold(it) } }
        }
            .onFailure { logger.error("Hente fra Api V2 feilet", it) }
            .getOrNull()
    }

    companion object {
        private val logger = getLogger(AaregService::class.java)
    }
}
