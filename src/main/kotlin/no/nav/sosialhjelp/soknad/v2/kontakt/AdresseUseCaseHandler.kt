package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.navenhet.NavEnhetService
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class AdresseUseCaseHandler(
    private val adresseService: AdresseService,
    private val navEnhetService: NavEnhetService,
    private val kommuneInfoService: KommuneInfoService,
    private val kodeverkService: KodeverkService,
) {
    fun getAdresseAndMottakerInfo(soknadId: UUID): AdresserDto {
        val adresser = adresseService.findAdresser(soknadId)
        val mottaker = adresseService.findMottaker(soknadId)
        val kommuneInfo = mottaker?.let { getKommuneInfo(soknadId, it) }

        return createAdresseDto(
            adresser = adresser,
            mottaker = mottaker?.toNavEnhetDto(kommuneInfo),
        )
    }

    fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
    ) {
        val oldAdresser = adresseService.findAdresser(soknadId)

        val mottaker =
            when (adresseValg) {
                AdresseValg.FOLKEREGISTRERT -> oldAdresser.folkeregistrert
                AdresseValg.MIDLERTIDIG -> oldAdresser.midlertidig
                AdresseValg.SOKNAD -> brukerAdresse
            }
                ?.let { valgtAdresse -> navEnhetService.getNavEnhet(personId(), valgtAdresse, adresseValg) }

        adresseService.updateBrukeradresse(soknadId, adresseValg, brukerAdresse, mottaker)
    }

    private fun getKommuneInfo(
        soknadId: UUID,
        navEnhet: NavEnhet,
    ): KommuneInfo {
        return navEnhet.kommunenummer?.let {
            KommuneInfo(
                isDigisosKommune = kommuneInfoService.kanMottaSoknader(it),
                kommunenavn = getKommunenavn(soknadId, navEnhet.kommunenavn, it),
            )
        } ?: error("NavEnhet ${navEnhet.enhetsnavn} mangler kommunenummer")
    }

    private fun getKommunenavn(
        soknadId: UUID,
        kommunenavn: String?,
        kommunenummer: String,
    ): String? {
        return kommunenavn
            ?: kodeverkService.getKommunenavn(kommunenummer)
                ?.also { adresseService.updateKommunenavnMottaker(soknadId, it) }
    }
}

data class KommuneInfo(
    val kommunenavn: String?,
    val isDigisosKommune: Boolean,
)
