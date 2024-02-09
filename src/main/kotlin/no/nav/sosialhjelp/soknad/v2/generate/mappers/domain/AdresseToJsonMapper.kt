package no.nav.sosialhjelp.soknad.v2.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.v2.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.adresse.AdresseRepository
import no.nav.sosialhjelp.soknad.v2.adresse.AdresseValg
import no.nav.sosialhjelp.soknad.v2.adresse.AdresserSoknad
import no.nav.sosialhjelp.soknad.v2.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.VegAdresse
import no.nav.sosialhjelp.soknad.v2.generate.DomainToJsonMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class AdresseToJsonMapper(
    private val adresseRepository: AdresseRepository
) : DomainToJsonMapper {
    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {

        val adresserSoknad = (
            adresseRepository.findByIdOrNull(soknadId)
                ?: throw IllegalStateException("Fant ikke Adresser")
            )

        doMapping(adresserSoknad, jsonInternalSoknad)
    }

    internal companion object AdresseMapper {

        fun doMapping(adresserSoknad: AdresserSoknad, json: JsonInternalSoknad) {
            val oppholdsadresse = adresserSoknad.getOppholdsadresse()
            val valgtAdresse = adresserSoknad.brukerInput?.valgtAdresse
                ?: throw IllegalStateException("Adressevalg er ikke satt for soknad")

            with(json) {
                initializeObjects()
                mapFolkeregistrertAdresse(adresserSoknad.folkeregistrertAdresse)
                mapMidlertidigAdresse(adresserSoknad.midlertidigAdresse)
                mapOppholdsadresse(oppholdsadresse, valgtAdresse)
            }
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            soknad.data.personalia ?: soknad.data.withPersonalia(JsonPersonalia())
        }

        private fun JsonInternalSoknad.mapFolkeregistrertAdresse(folkeregistrertAdresse: Adresse?) {
            folkeregistrertAdresse?.let {
                soknad.data.personalia.folkeregistrertAdresse = it.toJsonAdresse()
            }
        }

        private fun JsonInternalSoknad.mapMidlertidigAdresse(midlertidigAdresseSoknad: Adresse?) {
            midlertidigAdresseSoknad?.let {
                midlertidigAdresse = it.toJsonAdresse()
            }
        }

        private fun JsonInternalSoknad.mapOppholdsadresse(oppholdsadresse: Adresse, adresseValg: AdresseValg) {
            soknad.data.personalia.oppholdsadresse = oppholdsadresse.toJsonAdresse()
                .also {
                    it.kilde = if (adresseValg == AdresseValg.SOKNAD) { JsonKilde.BRUKER } else { JsonKilde.SYSTEM }

                    it.adresseValg = JsonAdresseValg.fromValue(adresseValg.name.lowercase())
                }
        }

        private fun Adresse.toJsonAdresse(): JsonAdresse {
            return when (this) {
                is VegAdresse -> toJsonGateAdresse()
                is MatrikkelAdresse -> toJsonMatrikkelAdresse()
                is UstrukturertAdresse -> toJsonUstrukturertAdresse()
                else -> throw IllegalStateException("Kan ikke mappe type ${this.javaClass} til adresse.")
            }
        }

        private fun VegAdresse.toJsonGateAdresse() = JsonGateAdresse()
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode(landkode)
            .withKommunenummer(kommunenummer)
            .withAdresselinjer(adresselinjer)
            .withBolignummer(bolignummer)
            .withPostnummer(postnummer)
            .withPoststed(poststed)
            .withGatenavn(gatenavn)
            .withHusnummer(husnummer)
            .withHusbokstav(husbokstav)

        private fun MatrikkelAdresse.toJsonMatrikkelAdresse() = JsonMatrikkelAdresse()
            .withType(JsonAdresse.Type.MATRIKKELADRESSE)
            .withKommunenummer(kommunenummer)
            .withGaardsnummer(gaardsnummer)
            .withBruksnummer(bruksnummer)
            .withFestenummer(festenummer)
            .withSeksjonsnummer(seksjonsnummer)
            .withUndernummer(undernummer)

        private fun UstrukturertAdresse.toJsonUstrukturertAdresse() = JsonUstrukturertAdresse()
            .withType(JsonAdresse.Type.USTRUKTURERT)
            .withAdresse(adresse)
    }
}
