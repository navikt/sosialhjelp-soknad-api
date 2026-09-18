package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
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
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad {
        eierRepository.findByIdOrNull(soknadId)?.let {
            return doMapping(it, jsonInternalSoknad)
        }
            ?: throw IllegalStateException("Fant ikke Eier")
    }

    internal companion object Mapper {
        fun doMapping(
            eier: Eier,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad {
            val soknad = requireNotNull(json.soknad)
            val data = requireNotNull(soknad.data)
            val personalia = requireNotNull(data.personalia)
            return json.copy(soknad = soknad.copy(data = data.copy(personalia = personalia.copy(navn = eier.navn.toJsonSokerNavn(), nordiskBorger = eier.toJsonNordiskBorger(), statsborgerskap = eier.toJsonStatsborgerskap(), kontonummer = requireNotNull(eier.kontonummer.toJsonKontonummer())))))
        }

        private fun Navn.toJsonSokerNavn(): JsonSokernavn =
            JsonSokernavn(JsonSokernavn.Kilde.SYSTEM, fornavn ?: "", mellomnavn ?: "", etternavn ?: "")

        private fun Eier.toJsonNordiskBorger(): JsonNordiskBorger? =
            nordiskBorger?.let {
                JsonNordiskBorger(JsonKilde.SYSTEM, nordiskBorger)
            }

        private fun Eier.toJsonStatsborgerskap(): JsonStatsborgerskap? =
            statsborgerskap?.let {
                JsonStatsborgerskap(JsonKilde.SYSTEM, it)
            }

        private fun Kontonummer.toJsonKontonummer(): JsonKontonummer? =
            when {
                harIkkeKonto == true ->
                    JsonKontonummer(JsonKilde.BRUKER, harIkkeKonto)
                fraBruker != null ->
                    JsonKontonummer(JsonKilde.BRUKER, false, fraBruker)
                // Merkelig nok skal HarIkkeKonto ikke være false når det er systemverdi
                fraRegister != null ->
                    JsonKontonummer(JsonKilde.SYSTEM, verdi = fraRegister)
                // Kontonummer kreves i modellen og kilde kreves selv uten(!!) noe informasjon
                else -> JsonKontonummer(JsonKilde.SYSTEM)
            }
    }
}
