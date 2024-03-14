package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.VegAdresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class KontaktToJsonMapper(
    private val kontaktRepository: KontaktRepository
) : DomainToJsonMapper {
    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {

        val kontakt = kontaktRepository.findByIdOrNull(soknadId)
            ?: throw IllegalStateException("Fant ikke Adresser")

        doMapping(kontakt, jsonInternalSoknad)
    }

    internal companion object Mapper {

        fun doMapping(kontakt: Kontakt, json: JsonInternalSoknad) {
            val oppholdsadresse = kontakt.adresser.getOppholdsadresse()
            val adresseValg = kontakt.adresser.adressevalg

            json.initializeObjects()
            json.midlertidigAdresse = kontakt.adresser.midlertidigAdresse?.toJsonAdresse()?.withKilde(JsonKilde.SYSTEM)

            with(json.soknad.data.personalia) {
                telefonnummer = kontakt.telefonnummer.toJsonTelefonnummer()
                folkeregistrertAdresse = kontakt.adresser.folkeregistrertAdresse?.toJsonAdresse()?.withKilde(JsonKilde.SYSTEM)
                adresseValg?.let { this.mapOppholdsadresse(oppholdsadresse, adresseValg) }
            }

            json.mottaker = kontakt.mottaker.toJsonSoknadsmottakerInternal()
            json.soknad.mottaker = kontakt.mottaker.toJsonSoknadsmottaker()
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            soknad.data.personalia ?: soknad.data.withPersonalia(JsonPersonalia())
        }

        private fun JsonPersonalia.mapOppholdsadresse(oppholdsadresse: Adresse, adresseValg: AdresseValg) {
            this.oppholdsadresse = oppholdsadresse.toJsonAdresse()
                .apply {
                    this.kilde = if (adresseValg == AdresseValg.SOKNAD) { JsonKilde.BRUKER } else { JsonKilde.SYSTEM }
                    this.adresseValg = JsonAdresseValg.fromValue(adresseValg.name.lowercase())
                }
        }

        private fun Telefonnummer.toJsonTelefonnummer(): JsonTelefonnummer? {
            return fraBruker?.let {
                JsonTelefonnummer()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(it)
            }
                ?: fraRegister?.let {
                    JsonTelefonnummer()
                        .withKilde(JsonKilde.BRUKER)
                        .withVerdi(it)
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

        private fun NavEnhet.toJsonSoknadsmottakerInternal(): JsonSoknadsmottaker? {
            return JsonSoknadsmottaker()
                .withOrganisasjonsnummer(orgnummer)
                .withNavEnhetsnavn(enhetsnavn)
        }

        private fun NavEnhet.toJsonSoknadsmottaker(): no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker? {
            return no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker()
                .withEnhetsnummer(enhetsnummer)
                .withKommunenummer(kommunenummer)
                .withNavEnhetsnavn(enhetsnavn)
        }
    }
}
