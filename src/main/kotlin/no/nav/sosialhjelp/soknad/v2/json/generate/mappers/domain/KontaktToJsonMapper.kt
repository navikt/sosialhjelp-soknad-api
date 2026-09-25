package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
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
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker as JsonSoknadsmottakerInternal

@Component
class KontaktToJsonMapper(
    private val kontaktRepository: KontaktRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad {
        val kontakt =
            kontaktRepository.findByIdOrNull(soknadId)
                ?: throw IllegalStateException("Fant ikke Adresser")

        return doMapping(kontakt, jsonInternalSoknad)
    }

    internal companion object Mapper {
        fun doMapping(
            kontakt: Kontakt,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad {
            val oppholdsadresse = kontakt.adresser.getOppholdsadresse()
            val adresseValg = kontakt.adresser.adressevalg

            val soknad = checkNotNull(json.soknad) { "SoknadToJsonMapper må kjøre først" }
            val personalia = soknad.data.personalia
            return json.copy(
                soknad =
                    soknad.copy(
                        data =
                            soknad.data.copy(
                                personalia =
                                    personalia.copy(
                                        telefonnummer = kontakt.telefonnummer.toJsonTelefonnummer(),
                                        folkeregistrertAdresse = kontakt.adresser.folkeregistrert?.toJsonAdresse(JsonKilde.SYSTEM),
                                        oppholdsadresse = adresseValg?.let { oppholdsadresse.toJsonAdresse(it.toJsonKilde(), JsonAdresseValg.fromValue(it.name.lowercase())) },
                                        postadresse = adresseValg?.let { oppholdsadresse.toJsonPostadresse(it) },
                                    ),
                            ),
                        mottaker = requireNotNull(kontakt.mottaker) { "Kontakt mangler mottaker" }.toJsonSoknadsmottaker(),
                    ),
                mottaker = kontakt.mottaker?.toJsonSoknadsmottakerInternal(),
                midlertidigAdresse = kontakt.adresser.midlertidig?.toJsonAdresse(JsonKilde.SYSTEM),
            )
        }

        private fun Adresse.toJsonPostadresse(valg: AdresseValg): JsonAdresse? = if (this is MatrikkelAdresse) null else toJsonAdresse(valg.toJsonKilde())

        private fun AdresseValg.toJsonKilde(): JsonKilde = if (this == AdresseValg.SOKNAD) JsonKilde.BRUKER else JsonKilde.SYSTEM

        private fun Telefonnummer.toJsonTelefonnummer(): JsonTelefonnummer? =
            fraBruker?.let {
                JsonTelefonnummer(JsonKilde.BRUKER, it)
            }
                ?: fraRegister?.let {
                    JsonTelefonnummer(JsonKilde.SYSTEM, it)
                }

        private fun Adresse.toJsonAdresse(
            kilde: JsonKilde,
            adresseValg: JsonAdresseValg? = null,
        ): JsonAdresse =
            when (this) {
                is VegAdresse -> toJsonGateAdresse(kilde, adresseValg)
                is MatrikkelAdresse -> toJsonMatrikkelAdresse(kilde, adresseValg)
                is UstrukturertAdresse -> toJsonUstrukturertAdresse(kilde, adresseValg)
                else -> throw IllegalStateException("Kan ikke mappe type ${this.javaClass} til adresse.")
            }

        private fun VegAdresse.toJsonGateAdresse(
            kilde: JsonKilde,
            adresseValg: JsonAdresseValg?,
        ) = JsonGateAdresse(kilde = kilde, landkode = landkode, kommunenummer = kommunenummer, adresselinjer = adresselinjer, bolignummer = bolignummer, postnummer = postnummer, poststed = poststed, gatenavn = gatenavn, husnummer = husnummer, husbokstav = husbokstav, adresseValg = adresseValg)

        private fun MatrikkelAdresse.toJsonMatrikkelAdresse(
            kilde: JsonKilde,
            adresseValg: JsonAdresseValg?,
        ) = JsonMatrikkelAdresse(kilde = kilde, kommunenummer = kommunenummer, gaardsnummer = gaardsnummer, bruksnummer = bruksnummer, festenummer = festenummer, seksjonsnummer = seksjonsnummer, undernummer = undernummer, adresseValg = adresseValg)

        private fun UstrukturertAdresse.toJsonUstrukturertAdresse(
            kilde: JsonKilde,
            adresseValg: JsonAdresseValg?,
        ) = JsonUstrukturertAdresse(kilde = kilde, adresse = adresse, adresseValg = adresseValg)

        // JsonSoknadsmottaer på dette nivået sendes ikke med til fiks
        private fun NavEnhet.toJsonSoknadsmottakerInternal(): JsonSoknadsmottakerInternal =
            JsonSoknadsmottakerInternal("", "$enhetsnavn, $kommunenavn")

        private fun NavEnhet.toJsonSoknadsmottaker(): JsonSoknadsmottaker =
            JsonSoknadsmottaker(kommunenummer = kommunenummer, enhetsnummer = enhetsnummer, navEnhetsnavn = "$enhetsnavn, $kommunenavn")
    }
}
