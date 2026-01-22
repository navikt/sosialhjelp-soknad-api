package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.arbeid.dto.toDomain
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import tools.jackson.module.kotlin.jacksonObjectMapper

@Component
class AaregService(
    private val aaregClient: AaregClient,
    private val organisasjonService: OrganisasjonService,
    private val aaregClientV2: AaregClientV2,
) {
    fun hentArbeidsforhold(): List<Arbeidsforhold>? {
        hentFraAaregV2HvisIkkeProd()

        return aaregClient.finnArbeidsforholdForArbeidstaker()
            ?.map { it.toDomain(organisasjonService) }
            .also { logger.info("Hentet ${it?.size ?: 0} arbeidsforhold fra aareg") }
    }

    private fun hentFraAaregV2HvisIkkeProd() {
        if (MiljoUtils.isProduction()) error("Skal ikke hente fra Aareg V2 i produksjon")

        logger.info("Henter arbeidsforhold for bruker fra aareg-api v2")
        runCatching {
            val arbeidsforholdDto = aaregClientV2.finnArbeidsforholdForArbeidstaker()
            logger.info("V2 Hentet arbeidsforhold: ${jacksonObjectMapper().writeValueAsString(arbeidsforholdDto)}")

            val arbeidsforholdCreator = ArbeidsforholdCreator(organisasjonService)

            val arbeidsforhold = arbeidsforholdDto?.map { arbeidsforholdCreator.createArbeidsforhold(it) }
            jacksonObjectMapper().writeValueAsString("Konverterte arbeidsforhold: $arbeidsforhold")
        }
            .onFailure { logger.error("Hente fra Api V2 feilet", it) }
            .getOrNull()
    }

    companion object {
        private val logger = getLogger(AaregService::class.java)
    }
}
