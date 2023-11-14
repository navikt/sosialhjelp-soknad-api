package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonKompatibilitet
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.*
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Eier
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Soknad
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.adresse.toTypedJsonAdresse
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.generell.toJsonSoknernavn
import org.springframework.stereotype.Component
import java.util.*

interface DomainToJsonMapper {
    fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad)
}

/**
 * Tiltenkt toppnivå for mapping. Kan utføre en del "må være på plass"-oppgaver før resten av mapperne kjører.
 */
@Component
class SoknadMapper (
    private val soknadRepository: SoknadRepository,
    private val mappers: List<DomainToJsonMapper> // Injecter alle som implementerer dette interfacet
) {
    fun mapSoknadToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val soknad = soknadRepository.findById(soknadId).get()
        soknad.mapToJson(jsonInternalSoknad)

        mappers.forEach { it.mapDomainToJson(soknadId, jsonInternalSoknad) }
    }

    private fun Soknad.mapToJson(json: JsonInternalSoknad) {
        json.soknad.let {
            it.innsendingstidspunkt = innsendingstidspunkt.toString()
            it.data.personalia = eier.toJsonPersonalia()
        }
    }
    private fun Eier.toJsonPersonalia() = JsonPersonalia()
        .withPersonIdentifikator(JsonPersonIdentifikator().withVerdi(personId))
        .withNavn(navn?.toJsonSoknernavn())
        .withStatsborgerskap(toJsonStatsborgerskap())
        .withNordiskBorger(toJsonNordiskBorger())
        .withFolkeregistrertAdresse(kontaktInfo?.folkeregistrertAdresse?.toTypedJsonAdresse())
        .withKontonummer(JsonKontonummer().withKilde(SYSTEM).withVerdi(kontonummer))
        .withTelefonnummer(JsonTelefonnummer().withKilde(SYSTEM))

    private fun Eier.toJsonStatsborgerskap() = JsonStatsborgerskap()
        .withKilde(SYSTEM)
        .withVerdi(statsborgerskap)

    private fun Eier.toJsonNordiskBorger() = JsonNordiskBorger()
        .withKilde(SYSTEM)
        .withVerdi(nordiskBorger)
}


