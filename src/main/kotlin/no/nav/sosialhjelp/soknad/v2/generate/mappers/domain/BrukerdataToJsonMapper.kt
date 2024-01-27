package no.nav.sosialhjelp.soknad.v2.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.v2.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.KontoInformasjonBruker
import no.nav.sosialhjelp.soknad.v2.brukerdata.Samtykke
import no.nav.sosialhjelp.soknad.v2.brukerdata.SamtykkeType
import no.nav.sosialhjelp.soknad.v2.generate.DomainToJsonMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class BrukerdataToJsonMapper(
    private val brukerdataRepository: BrukerdataRepository
) : DomainToJsonMapper {

    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        brukerdataRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
            ?: throw RuntimeException("Brukerdata finnes ikke")
    }

    internal companion object BrukerdataMapper {
        fun doMapping(brukerdata: Brukerdata, json: JsonInternalSoknad) {
            with(json) {
                initializeObjectsIfMissing()
                mapTelefonnummer(brukerdata)
                mapArbeid(brukerdata)
                mapKontonummer(brukerdata)
                mapBegrunnelse(brukerdata)
                mapSamtykker(brukerdata)
                mapBeskrivelseAvAnnet(brukerdata)
            }
        }

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

        // Hvis det finnes bruker-input, skal det overskrive hva enn som ligger uansett
        private fun JsonInternalSoknad.mapTelefonnummer(brukerdata: Brukerdata) {
            brukerdata.toJsonTelefonnummer()?.let {
                soknad.data.personalia.telefonnummer = it
            }
        }

        private fun Brukerdata.toJsonTelefonnummer(): JsonTelefonnummer? {
            return telefonnummer?.let {
                JsonTelefonnummer()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(it)
            }
        }

        private fun JsonInternalSoknad.mapArbeid(brukerdata: Brukerdata) {
            brukerdata.toJsonKommentarTilArbeidsforhold()?.let {
                soknad.data.arbeid.kommentarTilArbeidsforhold = it
            }
        }

        private fun Brukerdata.toJsonKommentarTilArbeidsforhold(): JsonKommentarTilArbeidsforhold? {
            return kommentarArbeidsforhold?.let {
                JsonKommentarTilArbeidsforhold().withVerdi(it)
            }
        }

        // Hvis det finnes bruker-input, skal det overskrive hva enn som ligger uansett
        private fun JsonInternalSoknad.mapKontonummer(brukerdata: Brukerdata) {
            brukerdata.kontoInformasjon?.toJsonKontonummer()?.let {
                soknad.data.personalia.kontonummer = it
            }
        }

        private fun KontoInformasjonBruker.toJsonKontonummer(): JsonKontonummer? {
            return if (kontonummer != null || harIkkeKonto != null) {
                JsonKontonummer()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(kontonummer)
                    .withHarIkkeKonto(harIkkeKonto)
            } else null
        }

        // disse finnes uansett, potensielt med null-verdier
        private fun JsonInternalSoknad.mapSamtykker(brukerdata: Brukerdata) {
            soknad.data.okonomi.opplysninger.bekreftelse
                .addAll(
                    brukerdata.samtykker.map {
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

        private fun JsonInternalSoknad.mapBegrunnelse(brukerdata: Brukerdata) {
            brukerdata.toJsonBegrunnelse()?.let {
                soknad.data.begrunnelse = it
            }
        }

        private fun Brukerdata.toJsonBegrunnelse(): JsonBegrunnelse? {
            return begrunnelse?.let {
                JsonBegrunnelse()
                    .withHvorforSoke(it.hvorforSoke)
                    .withHvaSokesOm(it.hvaSokesOm)
            }
        }

        private fun JsonInternalSoknad.mapBeskrivelseAvAnnet(brukerdata: Brukerdata) {
            brukerdata.toJsonOkonomibeskrivelseAvAnnet().let {
                soknad.data.okonomi.opplysninger.beskrivelseAvAnnet = it
            }
        }

        private fun Brukerdata.toJsonOkonomibeskrivelseAvAnnet(): JsonOkonomibeskrivelserAvAnnet? {
            return beskrivelseAvAnnet?.let {
                JsonOkonomibeskrivelserAvAnnet()
                    .withVerdi(it.verdier)
                    .withSparing(it.sparing)
                    .withBoutgifter(it.boutgifter)
                    .withUtbetaling(it.utbetalinger)
                    .withBarneutgifter(it.barneutgifter)
            }
        }
    }
}
