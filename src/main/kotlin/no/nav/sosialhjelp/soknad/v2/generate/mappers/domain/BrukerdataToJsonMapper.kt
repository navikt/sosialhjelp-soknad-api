package no.nav.sosialhjelp.soknad.v2.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.v2.brukerdata.Botype
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormelt
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormeltRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPerson
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPersonRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.KontoInformasjonBruker
import no.nav.sosialhjelp.soknad.v2.brukerdata.Samtykke
import no.nav.sosialhjelp.soknad.v2.brukerdata.SamtykkeType
import no.nav.sosialhjelp.soknad.v2.brukerdata.Studentgrad
import no.nav.sosialhjelp.soknad.v2.generate.DomainToJsonMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class BrukerdataToJsonMapper(
    private val brukerdataPersonRepository: BrukerdataPersonRepository,
    private val brukerdataFormeltRepository: BrukerdataFormeltRepository
) : DomainToJsonMapper {

    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        brukerdataFormeltRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }

        brukerdataPersonRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
    }

    internal companion object BrukerdataMapper {
        private fun JsonInternalSoknad.initializeObjectsIfMissing() {
            val jsonData = soknad.data ?: soknad.withData(JsonData()).data
            with(jsonData) {
                personalia ?: withPersonalia(JsonPersonalia())
                begrunnelse ?: withBegrunnelse(JsonBegrunnelse())
                arbeid ?: withArbeid(JsonArbeid())
                okonomi ?: withOkonomi(JsonOkonomi())
                okonomi.opplysninger ?: okonomi.withOpplysninger(JsonOkonomiopplysninger())
            }
        }

        fun doMapping(brukerdataFormelt: BrukerdataFormelt, json: JsonInternalSoknad) {
            with(json) {
                initializeObjectsIfMissing()
                mapArbeid(brukerdataFormelt)
                mapSamtykker(brukerdataFormelt)
                mapBeskrivelseAvAnnet(brukerdataFormelt)
                mapUtdanning(brukerdataFormelt)
            }
        }

        fun doMapping(brukerdataPerson: BrukerdataPerson, json: JsonInternalSoknad) {
            with(json) {
                initializeObjectsIfMissing()
                mapTelefonnummer(brukerdataPerson)
                mapBegrunnelse(brukerdataPerson)
                mapBosituasjon(brukerdataPerson)
                mapKontonummer(brukerdataPerson)
            }
        }

        // TODO Skal bruker-input på telefonnummer være gjeldende og absolutt uansett hva bruker ønsker?
        // TODO I søknaden er det ingen mulighet til å hente opp igjen systemregistrert telefonnummer.
        // Hvis det finnes bruker-input, skal det overskrive hva enn som ligger uansett
        private fun JsonInternalSoknad.mapTelefonnummer(brukerdataFormelt: BrukerdataPerson) {
            brukerdataFormelt.toJsonTelefonnummer()?.let {
                soknad.data.personalia.telefonnummer = it
            }
        }

        private fun BrukerdataPerson.toJsonTelefonnummer(): JsonTelefonnummer? {
            return telefonnummer?.let {
                JsonTelefonnummer()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(it)
            }
        }

        private fun JsonInternalSoknad.mapArbeid(brukerdataFormelt: BrukerdataFormelt) {
            brukerdataFormelt.toJsonKommentarTilArbeidsforhold()?.let {
                soknad.data.arbeid.kommentarTilArbeidsforhold = it
            }
        }

        private fun BrukerdataFormelt.toJsonKommentarTilArbeidsforhold(): JsonKommentarTilArbeidsforhold? {
            return kommentarArbeidsforhold?.let {
                JsonKommentarTilArbeidsforhold().withVerdi(it)
            }
        }

        // Hvis det finnes bruker-input, skal det overskrive hva enn som ligger uansett
        private fun JsonInternalSoknad.mapKontonummer(brukerdataPerson: BrukerdataPerson) {
            brukerdataPerson.kontoInformasjon?.let {
                soknad.data.personalia.kontonummer = it.toJsonKontonummer()
            }
        }

        // TODO Skal bruker-innfylt kontonummer være gjeldende og absolutt uanset hva bruker ønsker?
        // TODO I selve søknaden er det ingen mulighet til å hente opp igjen systemregistrert kontonummer.
        private fun KontoInformasjonBruker.toJsonKontonummer(): JsonKontonummer? {
            return if (kontonummer != null || harIkkeKonto != null) {
                JsonKontonummer()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(kontonummer)
                    .withHarIkkeKonto(harIkkeKonto)
            } else null
        }

        // disse finnes uansett, potensielt med null-verdier
        private fun JsonInternalSoknad.mapSamtykker(brukerdataFormelt: BrukerdataFormelt) {
            soknad.data.okonomi.opplysninger.bekreftelse
                .addAll(
                    brukerdataFormelt.samtykker.map {
                        it.toJsonOkonomibekreftelse()
                    }
                )
        }

        private fun Samtykke.toJsonOkonomibekreftelse() =
            JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withTittel(type.tittel)
                .withType(type.toSoknadJsonType())
                .withVerdi(verdi)
                .withBekreftelsesDato(dato.toString())

        internal fun SamtykkeType.toSoknadJsonType(): String {
            return when (this) {
                SamtykkeType.BOSTOTTE -> SoknadJsonTyper.BOSTOTTE_SAMTYKKE
                SamtykkeType.UTBETALING_SKATTEETATEN -> SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
            }
        }

        private fun JsonInternalSoknad.mapBegrunnelse(brukerdataPerson: BrukerdataPerson) {
            brukerdataPerson.toJsonBegrunnelse()?.let {
                soknad.data.begrunnelse = it
            }
        }

        private fun BrukerdataPerson.toJsonBegrunnelse(): JsonBegrunnelse? {
            return begrunnelse?.let {
                JsonBegrunnelse()
                    .withHvorforSoke(it.hvorforSoke)
                    .withHvaSokesOm(it.hvaSokesOm)
            }
        }

        private fun JsonInternalSoknad.mapBeskrivelseAvAnnet(brukerdataFormelt: BrukerdataFormelt) {
            soknad.data.okonomi.opplysninger.beskrivelseAvAnnet = brukerdataFormelt.toJsonOkonomibeskrivelseAvAnnet()
        }

        private fun BrukerdataFormelt.toJsonOkonomibeskrivelseAvAnnet(): JsonOkonomibeskrivelserAvAnnet? {
            return beskrivelseAvAnnet?.let {
                JsonOkonomibeskrivelserAvAnnet()
                    .withVerdi(it.verdier)
                    .withSparing(it.sparing)
                    .withBoutgifter(it.boutgifter)
                    .withUtbetaling(it.utbetalinger)
                    .withBarneutgifter(it.barneutgifter)
            }
        }

        private fun JsonInternalSoknad.mapUtdanning(brukerdataFormelt: BrukerdataFormelt) {
            soknad.data.utdanning = brukerdataFormelt.toJsonUtdanning()
        }

        private fun BrukerdataFormelt.toJsonUtdanning(): JsonUtdanning? {
            return utdanning?.let {
                JsonUtdanning()
                    .withKilde(JsonKilde.BRUKER)
                    .withErStudent(it.erStudent)
                    .withStudentgrad(it.studentGrad?.toJsonUtdanningStudentgrad())
            }
        }

        private fun Studentgrad.toJsonUtdanningStudentgrad() = JsonUtdanning.Studentgrad.fromValue(this.name.lowercase())

        private fun JsonInternalSoknad.mapBosituasjon(brukerdataPerson: BrukerdataPerson) {
            soknad.data.bosituasjon = brukerdataPerson.toJsonBosituasjon()
        }

        private fun BrukerdataPerson.toJsonBosituasjon(): JsonBosituasjon? {
            return bosituasjon?.let {
                JsonBosituasjon()
                    .withAntallPersoner(it.antallHusstand)
                    .withBotype(it.botype?.toJsonBosituasjonBotype())
            }
        }

        private fun Botype.toJsonBosituasjonBotype() = JsonBosituasjon.Botype.fromValue(name.lowercase())
    }
}
