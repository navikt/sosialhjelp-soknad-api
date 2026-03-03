package no.nav.sosialhjelp.soknad.v2.navenhet

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService.Companion.BYDEL_MARKA_OSLO
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import org.springframework.stereotype.Service

@Service
class NavEnhetService(
    private val norgService: NorgService,
    private val bydelFordelingService: BydelFordelingService,
    private val kodeverkService: KodeverkService,
) {
    fun findNavEnhetByAdresse(
        adresse: Adresse,
    ): NavEnhet {
        val navEnhet =
            adresse.checkBydelFordelingMarka()
                .let { norgService.getEnhetForGt(it) }
                ?: error("Fant ingen Nav-enhet for gt: ${adresse.getGtFromAdresse()}")

        log.info("Fant Nav-enhet ${navEnhet.enhetsnavn} (${navEnhet.enhetsnummer}) for gt: ${adresse.getGtFromAdresse()}")

        return navEnhet.copy(
            kommunenummer = adresse.getKommunenummer(),
            kommunenavn = getKommunenavn(adresse.getKommunenummer()),
        )
    }

    private fun getKommunenavn(kommunenummer: String): String? = kodeverkService.getKommunenavn(kommunenummer)

    private fun Adresse.checkBydelFordelingMarka(): String {
        if (this !is VegAdresse) return getGtFromAdresse() ?: error("Adresse mangler geografisk tilknytning")

        return when (BYDEL_MARKA_OSLO == getGtFromAdresse()) {
            true -> bydelFordelingService.getBydelTilForMarka(this)
            false -> getGtFromAdresse() ?: error("AdresseForslag mangler geografisk tilknytning")
        } ?: error("Adresse mangler geografisk tilknytning")
    }

    companion object {
        private val log by logger()
    }
}

private fun Adresse.getKommunenummer(): String {
    return when (this) {
        is VegAdresse -> kommunenummer
        is MatrikkelAdresse -> kommunenummer
        else -> error("Adresse av type ${this::class.simpleName} støttes ikke")
    }
        ?: error("Adresse mangler kommunenummer")
}

fun Adresse.getGtFromAdresse(): String? =
    when (this) {
        is VegAdresse -> bydelsnummer ?: kommunenummer
        is MatrikkelAdresse -> bydelsnummer ?: kommunenummer
        else -> null
    }
