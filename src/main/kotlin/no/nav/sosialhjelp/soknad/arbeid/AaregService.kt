package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsstedType
import no.nav.sosialhjelp.soknad.arbeid.dto.IdentInfoType
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import org.springframework.stereotype.Component

@Component
class AaregService(
    private val organisasjonService: OrganisasjonService,
    private val aaregClient: AaregClient,
) {
    fun hentArbeidsforhold(): List<Arbeidsforhold>? {
        logger.info("Henter arbeidsforhold for bruker fra Aareg-api")

        return runCatching {
            aaregClient.finnArbeidsforholdForArbeidstaker()
                ?.let { dto -> dto.map { it.createArbeidsforhold() } }
        }
            .onFailure { logger.error("Hente fra Aareg-api feilet", it) }
            .getOrThrow()
    }

    private fun ArbeidsforholdDto.createArbeidsforhold(): Arbeidsforhold {
        val orgnummerArbeidssted = getOrgnummerArbeidssted()
        val arbeidsstedNavn = createArbeidsstedNavn(orgnummerArbeidssted)
        val startdato = ansettelsesperiode.startdato
        val sluttdato = ansettelsesperiode.sluttdato

        val ansettelsesdetalj = getGjeldendeAnsettelsesdetalj()
        val stillingsprosent = ansettelsesdetalj?.avtaltStillingsprosent
        val harFastStilling = ansettelsesdetalj?.ansettelsesform?.kode == "fast"

        return Arbeidsforhold(
            orgnummer = orgnummerArbeidssted,
            arbeidsgivernavn = arbeidsstedNavn,
            start = startdato,
            slutt = sluttdato,
            fastStillingsprosent = stillingsprosent,
            harFastStilling = harFastStilling,
        )
    }

    private fun ArbeidsforholdDto.getOrgnummerArbeidssted(): String? {
        return arbeidssted
            .takeIf { ArbeidsstedType.Underenhet == it?.type }
            ?.identer?.find { IdentInfoType.ORGANISASJONSNUMMER == it.type }
            ?.ident
    }

    private fun ArbeidsforholdDto.createArbeidsstedNavn(orgnummer: String?): String =
        when {
            arbeidssted?.type == ArbeidsstedType.Person -> "Privatperson"
            orgnummer != null -> organisasjonService.hentOrgNavn(orgnummer)
            else -> ""
        }

    // det "gjeldende" ansettelsesdetaljen avgjøres avgjøres av rapporteringsmaaneder.til er null
    private fun ArbeidsforholdDto.getGjeldendeAnsettelsesdetalj() =
        ansettelsesdetaljer?.find { it.rapporteringsmaaneder?.til == null }

    companion object {
        private val logger by logger()
    }
}
