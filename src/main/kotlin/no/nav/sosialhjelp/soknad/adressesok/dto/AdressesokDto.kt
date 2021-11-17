package no.nav.sosialhjelp.soknad.adressesok.dto

import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.AdresseHelper
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper

data class AdressesokDataDto(
    val sokAdresse: AdressesokResultDto
)

data class AdressesokResultDto(
    val hits: List<AdressesokHitDto>?,
    val pageNumber: Int,
    val totalPages: Int,
    val totalHits: Int
)

data class AdressesokHitDto(
    val vegadresse: VegadresseDto,
    val score: Float
)

data class VegadresseDto(
    val matrikkelId: String,
    val husnummer: Int?,
    val husbokstav: String?,
    val adressenavn: String?,
    val kommunenavn: String?,
    val kommunenummer: String?,
    val postnummer: String?,
    val poststed: String?,
    val bydelsnummer: String?
) {
    fun toAdresseForslag(): AdresseForslag {
        return AdresseForslag(
            adresse = adressenavn,
            husnummer = husnummer.toString(),
            husbokstav = husbokstav,
            kommunenummer = kommunenummer,
            kommunenavn = kommunenavnFormattert,
            postnummer = postnummer,
            poststed = poststed,
            geografiskTilknytning = bydelsnummerOrKommunenummer,
            type = AdresseForslagType.GATEADRESSE
        )
    }

    private val kommunenavnFormattert: String get() = KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(
        kommunenummer,
        AdresseHelper.formatterKommunenavn(kommunenavn)
    )

    private val bydelsnummerOrKommunenummer: String? get() = bydelsnummer ?: kommunenummer
}
