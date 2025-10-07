package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SoknadValidator(
    private val adresseService: AdresseService,
    private val kommuneInfoService: KommuneInfoService,
) {
    fun validateAndReturnMottaker(soknadId: UUID): NavEnhet {
        return harSoknadMottaker(soknadId)
            .also { kanKommuneMottaSoknad(it) }
    }

    private fun harSoknadMottaker(soknadId: UUID): NavEnhet =
        adresseService.findMottaker(soknadId)
            ?.also { if (it.kommunenummer == null) error("Mottaker Mangler kommunenummer") }
            ?: error("Søknad mangler NavEnhet")

    private fun kanKommuneMottaSoknad(mottaker: NavEnhet) {
        val kommunenummer = mottaker.kommunenummer ?: error("NavEnhet ${mottaker.enhetsnavn} mangler kommunenummer")
        kommuneInfoService.hentAlleKommuneInfo()
            ?.let { kommuneInfoMap -> kommuneInfoMap[kommunenummer] }
            ?.also { kommuneInfo ->
                kommuneInfo.validateKanMottaSoknader()
                kommuneInfo.validateIsNotMidlertidigDeaktivert()
            }
            ?: throw SendingTilKommuneUtilgjengeligException("Fant ikke KommuneInfo for $kommunenummer")
    }
}

private fun KommuneInfo.validateKanMottaSoknader() {
    if (!kanMottaSoknader) {
        throw SendingTilKommuneUtilgjengeligException(
            "Kommune $kommunenummer kan ikke ta imot søknader",
        )
    }
}

private fun KommuneInfo.validateIsNotMidlertidigDeaktivert() {
    if (harMidlertidigDeaktivertMottak) {
        throw SendingTilKommuneErMidlertidigUtilgjengeligException(
            "Kommune $kommunenummer har midlertidig deaktivert mottak av søknader",
        )
    }
}
