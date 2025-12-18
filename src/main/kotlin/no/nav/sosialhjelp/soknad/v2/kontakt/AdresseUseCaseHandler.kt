package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.navenhet.NavEnhetService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AdresseUseCaseHandler(
    private val adresseService: AdresseService,
    private val navEnhetService: NavEnhetService,
    private val kommuneInfoService: KommuneInfoService,
    private val kodeverkService: KodeverkService,
    private val kortSoknadUseCaseHandler: KortSoknadUseCaseHandler,
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
        val currentAdresser = adresseService.findAdresser(soknadId)
        val currentMottaker = adresseService.findMottaker(soknadId)

        val mottaker =
            when (adresseValg) {
                AdresseValg.FOLKEREGISTRERT -> currentAdresser.folkeregistrert
                AdresseValg.MIDLERTIDIG -> currentAdresser.midlertidig
                AdresseValg.SOKNAD -> brukerAdresse
            }
                ?.let { navEnhetService.getNavEnhet(it) }
                ?: return

        runCatching { adresseService.updateAdresse(soknadId, adresseValg, brukerAdresse, mottaker) }
            .onSuccess { kortSoknadUseCaseHandler.resolveKortSoknad(soknadId, currentAdresser, currentMottaker, mottaker) }
    }

    private fun getKommuneInfo(
        soknadId: UUID,
        navEnhet: NavEnhet,
    ): RelevantKommuneInfo {
        return navEnhet.kommunenummer
            ?.let {
                val kommuneInfo = kommuneInfoService.hentAlleKommuneInfo()?.get(it)

                RelevantKommuneInfo(
                    kanMottaSoknader = kommuneInfo?.kanMottaSoknader ?: false,
                    isMidlertidigDeaktivert = kommuneInfo?.harMidlertidigDeaktivertMottak ?: true,
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

data class RelevantKommuneInfo(
    val kommunenavn: String?,
    val kanMottaSoknader: Boolean,
    val isMidlertidigDeaktivert: Boolean,
)
