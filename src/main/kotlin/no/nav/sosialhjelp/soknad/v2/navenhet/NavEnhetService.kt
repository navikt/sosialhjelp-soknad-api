package no.nav.sosialhjelp.soknad.v2.navenhet

import io.getunleash.Unleash
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService.Companion.BYDEL_MARKA_OSLO
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NavEnhetService(
    private val finnAdresseService: FinnAdresseService,
    private val unleash: Unleash,
    private val geografiskTilknytningService: GeografiskTilknytningService,
    private val norgService: NorgService,
    private val bydelFordelingService: BydelFordelingService,
    private val kommuneInfoService: KommuneInfoService,
    private val kodeverkService: KodeverkService,
) {
    fun getNavEnhet(
        soknadId: UUID,
        adresse: Adresse,
        valg: AdresseValg?,
    ): NavEnhet? {
        if (valg == AdresseValg.FOLKEREGISTRERT) {
            runCatching { return finnNavEnhetFraGT(soknadId, getKommunenummer(adresse)) }
                .onFailure { log.error("Kunne ikke hente NavEnhet fra GT", it) }
        }
        log.info("Finner Nav-enhet fra adressesøk")
        return finnNavEnhetFraAdresse(adresse)
    }

    private fun getKommunenummer(adresse: Adresse?): String? =
        when {
            isTestEnv -> {
                log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i prod")
                "3002"
            }
            adresse is VegAdresse -> adresse.kommunenummer
            adresse is MatrikkelAdresse -> adresse.kommunenummer
            else -> null
        }

    private val isTestEnv get() =
        MiljoUtils.isNonProduction() &&
            unleash.isEnabled(FEATURE_SEND_TIL_NAV_TESTKOMMUNE, false)

    private fun finnNavEnhetFraGT(
        soknadId: UUID,
        kommunenummer: String?,
    ): NavEnhet {
        log.info("Finner Nav-enhet fra GT")
        // gt er 4 sifret kommunenummer eller 6 sifret bydelsnummer
        val navEnhet =
            geografiskTilknytningService.hentGeografiskTilknytning(soknadId)
                ?.let { gt -> norgService.getEnhetForGt(gt) }

        val kommunenavn = kommunenummer?.let { getBehandlingskommune(it) }

        return NavEnhet(navEnhet?.enhetsnavn, navEnhet?.enhetsnummer, kommunenummer, kommunenavn = kommunenavn)
    }

    private fun finnNavEnhetFraAdresse(
        adresse: Adresse,
    ): NavEnhet? {
        log.info("Finner Nav-enhet fra adresse")

        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(adresse) ?: return null

        val navEnhet =
            adresseForslag
                .getGeografiskTilknytning()
                ?.let { gt -> norgService.getEnhetForGt(gt) }

        val kommunenavn = adresseForslag.kommunenummer?.let { getBehandlingskommune(it) }

        return NavEnhet(navEnhet?.enhetsnavn, navEnhet?.enhetsnummer, adresseForslag.kommunenummer, navEnhet?.orgnummer, kommunenavn)
    }

    private fun getBehandlingskommune(kommunenummer: String): String? {
        return getKommuneInfo(kommunenummer)?.behandlingsansvarlig
            ?.let { if (it.endsWith(" kommune")) it.replace(" kommune", "") else it }
            ?: kodeverkService.getKommunenavn(kommunenummer)
    }

    private fun getKommuneInfo(kommunenummer: String) =
        kommuneInfoService.hentAlleKommuneInfo()?.get(kommunenummer)

    private fun AdresseForslag.getGeografiskTilknytning() =
        when (BYDEL_MARKA_OSLO == geografiskTilknytning) {
            true -> bydelFordelingService.getBydelTilForMarka(this)
            false -> geografiskTilknytning
        }

    companion object {
        private val log by logger()
        const val FEATURE_SEND_TIL_NAV_TESTKOMMUNE = "sosialhjelp.soknad.send-til-nav-testkommune"
    }
}
