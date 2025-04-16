package no.nav.sosialhjelp.soknad.v2.navenhet

import io.getunleash.Unleash
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.navenhet.GeografiskTilknytning
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
            MiljoUtils.isNonProduction() &&
                unleash.isEnabled(FEATURE_SEND_TIL_NAV_TESTKOMMUNE, false) -> {
                log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i prod")
                "3002"
            }
            adresse is VegAdresse -> {
                adresse.kommunenummer
            }
            adresse is MatrikkelAdresse -> {
                adresse.kommunenummer
            }
            else -> {
                null
            }
        }

    private fun finnNavEnhetFraGT(
        soknadId: UUID,
        kommunenummer: String?,
    ): NavEnhet {
        log.info("Finner Nav-enhet fra GT")
        // gt er 4 sifret kommunenummer eller 6 sifret bydelsnummer
        val navEnhet =
            geografiskTilknytningService.hentGeografiskTilknytning(soknadId)
                ?.let { gt -> norgService.getEnhetForGt(gt) }

        val kommunenavn = kommunenummer?.let { kommuneInfoService.getBehandlingskommune(it) }

        return NavEnhet(navEnhet?.enhetsnavn, navEnhet?.enhetsnummer, kommunenummer, navEnhet?.orgnummer, kommunenavn)
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

        val kommunenavn = adresseForslag.kommunenummer?.let { kommuneInfoService.getBehandlingskommune(it) }

        return NavEnhet(navEnhet?.enhetsnavn, navEnhet?.enhetsnummer, adresseForslag.kommunenummer, navEnhet?.orgnummer, kommunenavn)
    }

    private fun AdresseForslag.getGeografiskTilknytning() =
        when (BYDEL_MARKA_OSLO == geografiskTilknytning) {
            true -> bydelFordelingService.getBydelTilForMarka(this)
            false -> geografiskTilknytning
        }?.let { GeografiskTilknytning(it) }

    companion object {
        private val log by logger()
        const val FEATURE_SEND_TIL_NAV_TESTKOMMUNE = "sosialhjelp.soknad.send-til-nav-testkommune"
    }
}
