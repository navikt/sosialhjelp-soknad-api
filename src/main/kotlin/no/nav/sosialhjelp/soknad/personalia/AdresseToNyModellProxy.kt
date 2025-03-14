package no.nav.sosialhjelp.soknad.personalia

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontendInput
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.GateadresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.MatrikkeladresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.UstrukturertAdresseFrontend
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseController
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseInput
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserDto
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserInput
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhetDto
import no.nav.sosialhjelp.soknad.v2.kontakt.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AdresseToNyModellProxy(
    private val adresseController: AdresseController,
) {
    fun getAdresser(
        soknadId: String,
    ): AdresserFrontend {
        return adresseController.getAdresser(UUID.fromString(soknadId)).toAdresserFrontend()
    }

    fun updateAdresse(
        soknadId: String,
        adresser: AdresserFrontendInput,
    ): List<NavEnhetFrontend> {
        return adresser.valg?.let {
            updateBrukerAdresse(soknadId = UUID.fromString(soknadId), it, adresser.soknad)
        }
            ?: error("Adressevalg er påkrevd")
    }

    private fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: JsonAdresseValg,
        adresseSoknad: AdresseFrontend?,
    ): List<NavEnhetFrontend> {
        return AdresserInput(
            adresseValg = adresseValg.toAdresseValg(),
            brukerAdresse = adresseSoknad?.toAdresse(),
        )
            .let { adresseController.updateAdresser(soknadId, it) }
            .toNavEnhetFrontendList()
    }

    companion object {
        private val logger by logger()
    }
}

private fun AdresserDto.toNavEnhetFrontendList(): List<NavEnhetFrontend> {
    return this.navenhet?.let { listOf(it.toNavEnhetFrontend()) } ?: emptyList()
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

private fun AdresseFrontend.toAdresse(): AdresseInput {
    return when (type) {
        JsonAdresse.Type.GATEADRESSE ->
            this.gateadresse?.toV2Adresse()
                ?: throw IllegalStateException("Gateadresse mangler i input")

        else -> throw IllegalStateException("Ukjent/ikke støttet adresse-type: $type")
    }
}

private fun GateadresseFrontend.toV2Adresse(): AdresseInput {
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

private fun AdresserDto.toAdresserFrontend(): AdresserFrontend {
    return AdresserFrontend(
        valg = this.adresseValg?.let { JsonAdresseValg.valueOf(it.name) },
        folkeregistrert = this.folkeregistrertAdresse?.toAdresseFrontend(),
        midlertidig = this.midlertidigAdresse?.toAdresseFrontend(),
        soknad = this.brukerAdresse?.toAdresseFrontend(),
        navEnhet = this.navenhet?.toNavEnhetFrontend(),
    )
}

private fun Adresse.toAdresseFrontend(): AdresseFrontend {
    return when (this) {
        is VegAdresse -> {
            AdresseFrontend(
                type = JsonAdresse.Type.GATEADRESSE,
                gateadresse = this.toGateadresseFrontend(),
            )
        }
        is MatrikkelAdresse -> {
            AdresseFrontend(
                type = JsonAdresse.Type.MATRIKKELADRESSE,
                matrikkeladresse = this.toMatrikkeladresseFrontend(),
            )
        }
        is UstrukturertAdresse -> {
            AdresseFrontend(
                type = JsonAdresse.Type.USTRUKTURERT,
                ustrukturert = this.toUstrukturertAdresseFrontend(),
            )
        }
        else -> throw IllegalArgumentException("Ukjent adresse")
    }
}

private fun VegAdresse.toGateadresseFrontend() =
    GateadresseFrontend(
        landkode = this.landkode,
        kommunenummer = this.kommunenummer,
        adresselinjer = this.adresselinjer,
        bolignummer = this.bolignummer,
        postnummer = this.postnummer,
        poststed = this.poststed,
        gatenavn = this.gatenavn,
        husnummer = this.husnummer,
        husbokstav = this.husbokstav,
    )

private fun MatrikkelAdresse.toMatrikkeladresseFrontend() =
    MatrikkeladresseFrontend(
        kommunenummer = this.kommunenummer,
        gaardsnummer = this.gaardsnummer,
        bruksnummer = this.bruksnummer,
        festenummer = this.festenummer,
        seksjonsnummer = this.seksjonsnummer,
        undernummer = this.undernummer,
    )

private fun UstrukturertAdresse.toUstrukturertAdresseFrontend() =
    UstrukturertAdresseFrontend(
        adresse = this.adresse,
    )

private fun NavEnhetDto.toNavEnhetFrontend() =
    NavEnhetFrontend(
        orgnr = this.orgnummer,
        enhetsnr = this.enhetsnummer,
        enhetsnavn = this.enhetsnavn ?: "",
        kommunenavn = this.kommunenavn,
        kommuneNr = this.kommunenummer,
        behandlingsansvarlig = null,
        valgt = null,
        isMottakMidlertidigDeaktivert = this.isMottakMidlertidigDeaktivert,
        isMottakDeaktivert = this.isMottakDeaktivert,
    )
