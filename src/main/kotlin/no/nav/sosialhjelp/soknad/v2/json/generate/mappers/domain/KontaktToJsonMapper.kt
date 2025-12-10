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
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KontaktToJsonMapper(
    private val kontaktRepository: KontaktRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        val kontakt =
            kontaktRepository.findByIdOrNull(soknadId)
                ?: throw IllegalStateException("Fant ikke Adresser")

        doMapping(kontakt, jsonInternalSoknad)
    }

    internal companion object Mapper {
        fun doMapping(
            kontakt: Kontakt,
            json: JsonInternalSoknad,
        ) {
            val oppholdsadresse = kontakt.adresser.getOppholdsadresse()
            val adresseValg = kontakt.adresser.adressevalg

            json.initializeObjects()
            json.midlertidigAdresse =
                kontakt.adresser.midlertidig
                    ?.toJsonAdresse()
                    ?.withKilde(JsonKilde.SYSTEM)

            with(json.soknad.data.personalia) {
                telefonnummer = kontakt.telefonnummer.toJsonTelefonnummer()
                folkeregistrertAdresse =
                    kontakt.adresser.folkeregistrert
                        ?.toJsonAdresse()
                        ?.withKilde(JsonKilde.SYSTEM)
                adresseValg?.also {
                    this.oppholdsadresse = oppholdsadresse.mapOppholdsadresse(it)
                    this.postadresse = oppholdsadresse.mapToPostadresse(it)
                }
            }

            json.mottaker = kontakt.mottaker?.toJsonSoknadsmottakerInternal()
            json.soknad.mottaker = kontakt.mottaker?.toJsonSoknadsmottaker()
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            soknad.data.personalia ?: soknad.data.withPersonalia(JsonPersonalia())
        }

        private fun Adresse.mapOppholdsadresse(
            adresseValg: AdresseValg,
        ): JsonAdresse =
            this
                .toJsonAdresse()
                .withKilde(if (adresseValg == AdresseValg.SOKNAD) JsonKilde.BRUKER else JsonKilde.SYSTEM)
                .withAdresseValg(JsonAdresseValg.fromValue(adresseValg.name.lowercase()))

        private fun Adresse.mapToPostadresse(valg: AdresseValg): JsonAdresse? =
            if (this is MatrikkelAdresse) {
                null
            } else {
                this.mapOppholdsadresse(valg).withAdresseValg(null)
            }

        private fun Telefonnummer.toJsonTelefonnummer(): JsonTelefonnummer? =
            fraBruker?.let {
                JsonTelefonnummer()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(it)
            }
                ?: fraRegister?.let {
                    JsonTelefonnummer()
                        .withKilde(JsonKilde.SYSTEM)
                        .withVerdi(it)
                }

        private fun Adresse.toJsonAdresse(): JsonAdresse =
            when (this) {
                is VegAdresse -> toJsonGateAdresse()
                is MatrikkelAdresse -> toJsonMatrikkelAdresse()
                is UstrukturertAdresse -> toJsonUstrukturertAdresse()
                else -> throw IllegalStateException("Kan ikke mappe type ${this.javaClass} til adresse.")
            }

        private fun VegAdresse.toJsonGateAdresse() =
            JsonGateAdresse()
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

        private fun MatrikkelAdresse.toJsonMatrikkelAdresse() =
            JsonMatrikkelAdresse()
                .withType(JsonAdresse.Type.MATRIKKELADRESSE)
                .withKommunenummer(kommunenummer)
                .withGaardsnummer(gaardsnummer)
                .withBruksnummer(bruksnummer)
                .withFestenummer(festenummer)
                .withSeksjonsnummer(seksjonsnummer)
                .withUndernummer(undernummer)

        private fun UstrukturertAdresse.toJsonUstrukturertAdresse() =
            JsonUstrukturertAdresse()
                .withType(JsonAdresse.Type.USTRUKTURERT)
                .withAdresse(adresse)

        // JsonSoknadsmottaer på dette nivået sendes ikke med til fiks
        private fun NavEnhet.toJsonSoknadsmottakerInternal(): JsonSoknadsmottaker? =
            JsonSoknadsmottaker()
                .withOrganisasjonsnummer("")
                .withNavEnhetsnavn("$enhetsnavn, $kommunenavn")

        private fun NavEnhet.toJsonSoknadsmottaker(): no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker? =
            no.nav.sbl.soknadsosialhjelp.soknad
                .JsonSoknadsmottaker()
                .withEnhetsnummer(enhetsnummer)
                .withKommunenummer(kommunenummer)
                .withNavEnhetsnavn("$enhetsnavn, $kommunenavn")
    }
}
