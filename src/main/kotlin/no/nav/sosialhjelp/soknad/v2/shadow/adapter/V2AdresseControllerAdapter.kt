package no.nav.sosialhjelp.soknad.v2.shadow.adapter

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.GateadresseFrontend
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.AdresseController
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.AdresserInput
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.VegAdresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class V2AdresseControllerAdapter(
    private val adresseController: AdresseController,
    private val kontaktRepository: KontaktRepository,
) {
    fun updateAdresse(soknadId: UUID, adresseValg: JsonAdresseValg, adresseSoknad: AdresseFrontend?,) {
        AdresserInput(
            adresseValg = adresseValg.toAdresseValg(),
            brukerAdresse = adresseSoknad?.toAdresse()
        )
            .let { adresseController.updateAdresser(soknadId, it) }
    }

    fun updateNavEnhet(soknadId: UUID, navEnhetFrontend: NavEnhetFrontend) {
        val kontakt = kontaktRepository.findByIdOrNull(soknadId) ?: Kontakt(soknadId)

        navEnhetFrontend.toNavEnhet()
            .let { navEnhet -> kontakt.copy(mottaker = navEnhet) }
            .also { updatedKontakt -> kontaktRepository.save(updatedKontakt) }
    }
}

private fun NavEnhetFrontend.toNavEnhet(): NavEnhet {
    return NavEnhet(
        kommunenummer = kommuneNr,
        kommunenavn = kommunenavn,
        enhetsnavn = enhetsnavn,
        enhetsnummer = enhetsnr,
        orgnummer = orgnr,
    )
}

private fun JsonAdresseValg.toAdresseValg(): AdresseValg {
    return when (this) {
        JsonAdresseValg.FOLKEREGISTRERT -> AdresseValg.FOLKEREGISTRERT
        JsonAdresseValg.MIDLERTIDIG -> AdresseValg.MIDLERTIDIG
        JsonAdresseValg.SOKNAD -> AdresseValg.SOKNAD
    }
}

private fun AdresseFrontend.toAdresse(): Adresse {
    return when (type) {
        JsonAdresse.Type.GATEADRESSE -> this.gateadresse?.toV2Adresse()
            ?: throw IllegalStateException("NyModell: Gateadresse mangler i input")

        else -> throw IllegalStateException("NyModell: Ukjent/ikke støttet adresse-type: $type")
    }
}

private fun GateadresseFrontend.toV2Adresse(): Adresse {
    return VegAdresse(
        kommunenummer = kommunenummer,
        adresselinjer = adresselinjer ?: emptyList(),
        bolignummer = bolignummer,
        postnummer = postnummer,
        poststed = poststed,
        gatenavn = gatenavn,
        husnummer = husnummer,
        husbokstav = husbokstav,
    )
}
