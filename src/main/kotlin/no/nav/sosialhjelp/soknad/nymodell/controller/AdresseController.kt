package no.nav.sosialhjelp.soknad.nymodell.controller

import no.nav.sosialhjelp.soknad.nymodell.controller.dto.AdresseObjectDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.AdresseRequest
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.AdresseResponse
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.GateadresseDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.UstrukturertAdresseDto
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg.FOLKEREGISTRERT
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg.MIDLERTIDIG
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseValg.SOKNAD
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.GateAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.MatrikkelAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.UstrukturertAdresseObject
import no.nav.sosialhjelp.soknad.nymodell.service.AdresseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/soknad/{soknadId}/adresse", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseController(
    private val adresseService: AdresseService
) {

    @GetMapping
    fun hentAdresser(
        @PathVariable("soknadId") soknadId: UUID
    ): AdresseResponse {
        val adresseMap = adresseService.hentAdresser(soknadId)
        val adresseValg = adresseService.hentAdresseValg(soknadId)

        return AdresseResponse(
            adresseValg = adresseValg,
            folkeregistrertAdresse = adresseMap[FOLKEREGISTRERT]?.toGateadresseDto(),
            oppholdsadresse = adresseMap[SOKNAD]?.toGateadresseDto(),
            midlertidigAdresse = adresseMap[MIDLERTIDIG]?.toGateadresseDto()
        )
    }

    @PutMapping
    fun updateAdresse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody adresseRequest: AdresseRequest
    ): AdresseResponse {
        adresseService.updateAdresseValg(soknadId, adresseRequest.valg)
        if (adresseRequest.valg == SOKNAD) {
            adresseService.updateOppholdsadresse(soknadId, adresseRequest.adresseSoknad.toDomain())
        }
        return hentAdresser(soknadId)
    }

    private fun AdresseObjectDto.toDomain(): AdresseObject {
        return when (this) {
            is GateadresseDto -> this.toGateadresseObject()
            is MatrikkeladresseDto -> this.toMatrikkeladresseObject()
            is UstrukturertAdresseDto -> UstrukturertAdresseObject(adresse = this.adresse)
            else -> throw IllegalStateException("Kan ikke mappe AdresseDto til AdresseObject")
        }
    }

    private fun GateadresseDto.toGateadresseObject() = GateAdresseObject(
        landkode = landkode,
        kommunenummer = kommunenummer,
        adresselinjer = adresselinjer,
        bolignummer = bolignummer,
        postnummer = postnummer,
        poststed = poststed,
        gatenavn = gatenavn,
        husnummer = husnummer,
        husbokstav = husbokstav
    )

    private fun MatrikkeladresseDto.toMatrikkeladresseObject() = MatrikkelAdresseObject(
        kommunenummer = kommunenummer,
        gaardsnummer = gaardsnummer,
        bruksnummer = bruksnummer,
        festenummer = festenummer,
        seksjonsnummer = seksjonsnummer,
        undernummer = undernummer
    )

    private fun AdresseObject.toGateadresseDto(): AdresseObjectDto {
        return when (this) {
            is GateAdresseObject -> this.toGateadresseDto()
            is MatrikkelAdresseObject -> this.toMatrikkeladresseDto()
            is UstrukturertAdresseObject -> UstrukturertAdresseDto(adresse = this.adresse)
            else -> throw IllegalStateException("Kan ikke mappe adresseobjekt til Dto")
        }
    }

    private fun GateAdresseObject.toGateadresseDto() = GateadresseDto(
        landkode = landkode,
        kommunenummer = kommunenummer,
        adresselinjer = adresselinjer,
        bolignummer = bolignummer,
        postnummer = postnummer,
        poststed = poststed,
        gatenavn = gatenavn,
        husnummer = husnummer,
        husbokstav = husbokstav
    )

    private fun MatrikkelAdresseObject.toMatrikkeladresseDto() = MatrikkeladresseDto(
        kommunenummer = kommunenummer,
        gaardsnummer = gaardsnummer,
        bruksnummer = bruksnummer,
        festenummer = festenummer,
        seksjonsnummer = seksjonsnummer,
        undernummer = undernummer
    )
}
