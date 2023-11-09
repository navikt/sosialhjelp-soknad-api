package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Eier
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Navn
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Soknad
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.repository.SoknadRepository
import org.springframework.stereotype.Component
import java.util.*

interface DomainToJsonMapper {
    fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad)
}

@Component
class SoknadMapper (
    private val soknadRepository: SoknadRepository,
    private val mappers: List<DomainToJsonMapper>
) {
    fun mapSoknadToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val soknad = soknadRepository.findById(soknadId).get()
        soknad.mapToJson(jsonInternalSoknad)

        mapToJsonInternalSoknad(soknadId, jsonInternalSoknad)
    }

    private fun mapToJsonInternalSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        mappers.forEach { it.mapDomainToJson(soknadId, jsonInternalSoknad) }
    }
}

fun Soknad.mapToJson(json: JsonInternalSoknad) {
    json.soknad
        .withInnsendingstidspunkt(innsendingstidspunkt.toString())
        .data
        .withBegrunnelse(toJsonBegrunnelse())
        .withPersonalia(eier.toJsonPersonalia())
}

fun Soknad.toJsonBegrunnelse() =JsonBegrunnelse()
//    .withHvorforSoke(hvorforSoke)
//    .withHvaSokesOm(hvaSokesOm)

fun Eier.toJsonPersonalia(): JsonPersonalia {
    return JsonPersonalia()
        .withPersonIdentifikator(JsonPersonIdentifikator().withVerdi(personId))
        .withNavn(navn?.toJsonSoknernavn())
        .withStatsborgerskap(toJsonStatsborgerskap())
        .withNordiskBorger(toJsonNordiskBorger())
}

fun Navn.toJsonSoknernavn() = JsonSokernavn()
    .withFornavn(fornavn)
    .withMellomnavn(mellomnavn)
    .withEtternavn(etternavn)

fun Eier.toJsonStatsborgerskap() = JsonStatsborgerskap()
    .withKilde(JsonKilde.SYSTEM)
    .withVerdi(statsborgerskap)

fun Eier.toJsonNordiskBorger() = JsonNordiskBorger()
    .withKilde(JsonKilde.SYSTEM)
    .withVerdi(nordiskBorger)