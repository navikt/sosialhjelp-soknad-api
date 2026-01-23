package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDtoV2
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsstedType
import no.nav.sosialhjelp.soknad.arbeid.dto.IdentInfoDto
import no.nav.sosialhjelp.soknad.arbeid.dto.IdentInfoType
import no.nav.sosialhjelp.soknad.arbeid.dto.OpplysningspliktigType
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import java.time.LocalDate

class ArbeidsforholdCreator(private val organisasjonService: OrganisasjonService) {
    fun createArbeidsforhold(dto: ArbeidsforholdDtoV2): Arbeidsforhold {
        val orgnummerOpplysningspliktig = dto.getOrgnummerOpplysningspliktig()
        val orgnummerArbeidssted = dto.getOrgnummerArbeidssted()
        val opplysningspliktigNavn = dto.createOpplysningspliktigNavn(orgnummerOpplysningspliktig)
        val arbeidsstedNavn = dto.createArbeidsstedNavn(orgnummerArbeidssted)
        val startdato = dto.getStartdato()
        val sluttdato = dto.getSluttdato()

        val ansettelsesdetalj = dto.getGjeldendeAnsettelsesdetalj()
        val stillingsprosent = ansettelsesdetalj?.avtaltStillingsprosent
        val harFastStilling = ansettelsesdetalj?.ansettelsesform?.kode == "fast"

        return Arbeidsforhold(
            orgnummer = "opplysningspliktig: $orgnummerOpplysningspliktig, arbeidssted: $orgnummerArbeidssted",
            arbeidsgivernavn = "opplysningspliktig: $opplysningspliktigNavn, arbeidssted: $arbeidsstedNavn",
            start = startdato,
            slutt = sluttdato,
            fastStillingsprosent = stillingsprosent,
            harFastStilling = harFastStilling,
        )
    }

    private fun ArbeidsforholdDtoV2.getOrgnummerOpplysningspliktig(): String? {
        return opplysningspliktig
            .takeIf { OpplysningspliktigType.Hovedenhet == it?.type }
            ?.identer?.findOrgnummerType()
            ?.ident
    }

    private fun ArbeidsforholdDtoV2.getOrgnummerArbeidssted(): String? {
        return arbeidssted
            .takeIf { ArbeidsstedType.Underenhet == it?.type }
            ?.identer?.findOrgnummerType()
            ?.ident
    }

    private fun List<IdentInfoDto>.findOrgnummerType(): IdentInfoDto? =
        find { IdentInfoType.ORGANISASJONSNUMMER == it.type }

    private fun ArbeidsforholdDtoV2.createOpplysningspliktigNavn(orgnummer: String?): String =
        when {
            opplysningspliktig?.type == OpplysningspliktigType.Person -> "Privatperson"
            orgnummer != null -> organisasjonService.hentOrgNavn(orgnummer)
            else -> ""
        }

    private fun ArbeidsforholdDtoV2.createArbeidsstedNavn(orgnummer: String?): String =
        when {
            arbeidssted?.type == ArbeidsstedType.Person -> "Privatperson"
            orgnummer != null -> organisasjonService.hentOrgNavn(orgnummer)
            else -> ""
        }

    private fun ArbeidsforholdDtoV2.getStartdato(): LocalDate = ansettelsesperiode.startdato

    private fun ArbeidsforholdDtoV2.getSluttdato(): LocalDate? = ansettelsesperiode.sluttdato

    // det "gjeldende" ansettelsesdetaljen avgjøres avgjøres av rapporteringsmaaneder.til er null
    private fun ArbeidsforholdDtoV2.getGjeldendeAnsettelsesdetalj() =
        ansettelsesdetaljer?.find { it.rapporteringsmaaneder?.til == null }
}
