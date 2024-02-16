package no.nav.sosialhjelp.soknad.v2.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.eier.Navn
import no.nav.sosialhjelp.soknad.v2.generate.DomainToJsonMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class EierToJsonMapper(
    private val eierRepository: EierRepository
) : DomainToJsonMapper {
    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {

        val eier = (
            eierRepository.findByIdOrNull(soknadId)
                ?: throw IllegalStateException("Fant ikke Eier")
            )

        doMapping(eier, jsonInternalSoknad)
    }

    internal companion object Mapper {

        fun doMapping(eier: Eier, json: JsonInternalSoknad) {

            json.initializeObjects()
            with(json.soknad.data.personalia) {

                this.navn = eier.navn.toJsonSokerNavn()
                this.nordiskBorger = eier.toJsonNordiskBorger()
                this.statsborgerskap = eier.toJsonStatsborgerskap()
                this.kontonummer = eier.kontonummer.toJsonKontonummer()
            }
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            soknad.data.personalia ?: soknad.data.withPersonalia(JsonPersonalia())
        }

        private fun Navn.toJsonSokerNavn(): JsonSokernavn {
            return JsonSokernavn()
                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                .withFornavn(fornavn)
                .withMellomnavn(mellomnavn)
                .withEtternavn(etternavn)
        }

        private fun Eier.toJsonNordiskBorger(): JsonNordiskBorger? {
            return nordiskBorger?.let {
                JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(nordiskBorger)
            }
        }

        private fun Eier.toJsonStatsborgerskap(): JsonStatsborgerskap? {
            return statsborgerskap?.let {
                JsonStatsborgerskap().withKilde(JsonKilde.BRUKER).withVerdi(it)
            }
        }

        private fun Kontonummer.toJsonKontonummer(): JsonKontonummer? {
            return when {
                harIkkeKonto == true ->
                    JsonKontonummer().withKilde(JsonKilde.BRUKER).withHarIkkeKonto(true)
                bruker != null ->
                    JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi(bruker)
                register != null ->
                    JsonKontonummer().withKilde(JsonKilde.SYSTEM).withVerdi(register)
                else -> null
            }
        }
    }
}
