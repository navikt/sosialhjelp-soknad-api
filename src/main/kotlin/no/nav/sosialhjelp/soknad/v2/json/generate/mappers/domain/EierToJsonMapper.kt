package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

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
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EierToJsonMapper(
    private val eierRepository: EierRepository,
) : DomainToJsonMapper {
    override fun mapToSoknad(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        eierRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
            ?: throw IllegalStateException("Fant ikke Eier")
    }

    internal companion object Mapper {
        fun doMapping(
            eier: Eier,
            json: JsonInternalSoknad,
        ) {
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
            soknad.data.personalia.kontonummer ?: soknad.data.personalia.withKontonummer(JsonKontonummer())
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
                JsonStatsborgerskap().withKilde(JsonKilde.SYSTEM).withVerdi(it)
            }
        }

        private fun Kontonummer.toJsonKontonummer(): JsonKontonummer {
            return when {
                harIkkeKonto == true ->
                    JsonKontonummer().withKilde(JsonKilde.BRUKER).withHarIkkeKonto(harIkkeKonto)
                fraBruker != null ->
                    JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi(fraBruker)
                fraRegister != null ->
                    JsonKontonummer().withKilde(JsonKilde.SYSTEM).withVerdi(fraRegister)
                // TODO I den gamle logikken opprettes kontonummer som et tomt objekt med kilde = system... Riktig?
                else -> JsonKontonummer().withKilde(JsonKilde.SYSTEM)
            }
        }
    }
}
