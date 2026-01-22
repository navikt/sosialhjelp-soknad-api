package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDtoV2
import no.nav.sosialhjelp.soknad.arbeid.dto.OpplysningspliktigDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OpplysningspliktigIdentDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OpplysningspliktigIdentType
import no.nav.sosialhjelp.soknad.arbeid.dto.OpplysningspliktigType
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import java.time.LocalDate

class ArbeidsforholdCreator(private val organisasjonService: OrganisasjonService) {
    fun createArbeidsforhold(dto: ArbeidsforholdDtoV2): Arbeidsforhold {
        val orgnummer = dto.getOrgnummer()
        val arbeidsgivernavn = dto.createArbeidsgiverNavn(orgnummer)
        val startdato = dto.getStartdato()
        val sluttdato = dto.getSluttdato()
        val stillingsprosent = dto.getStillingsprosent()
        val harFastStilling = dto.harFastStilling()

        return Arbeidsforhold(
            orgnummer = orgnummer,
            arbeidsgivernavn = arbeidsgivernavn,
            start = startdato,
            slutt = sluttdato,
            fastStillingsprosent = stillingsprosent,
            harFastStilling = harFastStilling,
        )
    }

    private fun ArbeidsforholdDtoV2.getOrgnummer(): String? {
        return opplysningspliktig
            .takeIf { OpplysningspliktigType.Hovedenhet == it?.type }
            ?.findOrgnummerType()
            ?.ident
    }

    private fun OpplysningspliktigDto.findOrgnummerType(): OpplysningspliktigIdentDto? =
        identer.find { OpplysningspliktigIdentType.ORGANISASJONSNUMMER == it.type }

    private fun ArbeidsforholdDtoV2.createArbeidsgiverNavn(orgnummer: String?): String =
        when {
            opplysningspliktig?.type == OpplysningspliktigType.Person -> "Privatperson"
            orgnummer != null -> organisasjonService.hentOrgNavn(orgnummer)
            else -> ""
        }

    private fun ArbeidsforholdDtoV2.getStartdato(): LocalDate = ansettelsesperiode.startdato

    private fun ArbeidsforholdDtoV2.getSluttdato(): LocalDate? = ansettelsesperiode.sluttdato

    private fun ArbeidsforholdDtoV2.getStillingsprosent(): Double? =
        ansettelsesdetaljer?.sumOf { it.avtaltStillingsprosent }

    private fun ArbeidsforholdDtoV2.harFastStilling(): Boolean = ansettelsesdetaljer?.isNotEmpty() == true
}
