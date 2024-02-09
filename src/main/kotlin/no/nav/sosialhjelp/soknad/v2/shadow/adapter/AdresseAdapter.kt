package no.nav.sosialhjelp.soknad.v2.shadow.adapter

import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Matrikkeladresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Oppholdsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse
import no.nav.sosialhjelp.soknad.v2.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.adresse.AdresseRepository
import no.nav.sosialhjelp.soknad.v2.adresse.AdresserSoknad
import no.nav.sosialhjelp.soknad.v2.adresse.BrukerInputAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.VegAdresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class AdresseAdapter(
    private val adresseRepository: AdresseRepository,
    private val hentAdresseService: HentAdresseService
) {
    fun updateAdresserFraRegister(
        soknadId: UUID,
        folkeregistrertAdresse: Bostedsadresse?,
        midlertidigAdresse: Oppholdsadresse?
    ) {
        val adresserSoknad = AdresserSoknad(
            soknadId = soknadId,
            folkeregistrertAdresse = folkeregistrertAdresse?.toV2Adresse(),
            midlertidigAdresse = midlertidigAdresse?.toV2Adresse()
        )

        /* TODO I fremtiden ønsker vi å manipulere den lagrede entiteten direkte - men systemdata oppdateres litt i..
        ... hytt og pine så det må gjøre en replace av hele entiteten.
        */
        adresseRepository.findByIdOrNull(soknadId)?.brukerInput
            ?.let {
                adresserSoknad.brukerInput = BrukerInputAdresse(
                    valgtAdresse = it.valgtAdresse,
                    brukerAdresse = it.brukerAdresse
                )
            }

        adresseRepository.save(adresserSoknad)
    }

    private fun Bostedsadresse.toV2Adresse(): Adresse {
        return vegadresse?.toV2Vegadresse()
            ?: matrikkeladresse?.toV2Matrikkeladresse()
            ?: throw IllegalStateException("Ukjent bostedsadresse fra PDL (skal være Vegadresse eller Matrikkeladresse")
    }

    private fun Oppholdsadresse.toV2Adresse(): Adresse {
        return vegadresse?.toV2Vegadresse()
            ?: throw IllegalStateException("Ukjent oppholdsadresse fra PDL (skal være Vegadresse eller Matrikkeladresse")
    }

    private fun Vegadresse.toV2Vegadresse(): VegAdresse {
        return VegAdresse(
            husnummer = husnummer.toString(),
            husbokstav = husbokstav,
            kommunenummer = kommunenummer,
            postnummer = postnummer,
            adresselinjer = tilleggsnavn?.let { listOf(it) } ?: emptyList(),
            bolignummer = bruksenhetsnummer,
            poststed = poststed,
            gatenavn = adressenavn,
        )
    }

    private fun Matrikkeladresse.toV2Matrikkeladresse(): MatrikkelAdresse? {
        return matrikkelId?.let {
            hentAdresseService.hentKartverketMatrikkelAdresse(it)?.toV2Matrikkeladresse()
        }
    }

    private fun KartverketMatrikkelAdresse.toV2Matrikkeladresse(): MatrikkelAdresse {
        return MatrikkelAdresse(
            kommunenummer = kommunenummer!!,
            gaardsnummer = gaardsnummer!!,
            bruksnummer = bruksnummer!!,
            festenummer = festenummer,
            seksjonsnummer = seksjonsnummer,
            undernummer = undernummer
        )
    }
}

