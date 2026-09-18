package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

interface OkonomiElementsToJsonMapper {
    fun doMapping(jsonOkonomi: JsonOkonomi): JsonOkonomi
}

@Component
class OkonomiToJsonHandler(
    private val okonomiRepository: OkonomiRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad {
        val jsonOkonomi = jsonInternalSoknad.initializeObjects()
        // denne er satt til null pga kort soknad
        val jsonWithEmptyUtgifter = jsonOkonomi.copy(opplysninger = jsonOkonomi.opplysninger.copy(utgift = emptyList()))

        return okonomiRepository.findByIdOrNull(soknadId)
            ?.let { okonomi -> jsonInternalSoknad.withOkonomi(doMapping(okonomi, jsonWithEmptyUtgifter)) }
            ?: jsonInternalSoknad.withOkonomi(jsonWithEmptyUtgifter)
    }

    override fun mapToKortJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad {
        val jsonOkonomi = jsonInternalSoknad.initializeObjects()

        return okonomiRepository.findByIdOrNull(soknadId)
            ?.let { okonomi -> jsonInternalSoknad.withOkonomi(doKortMapping(okonomi, jsonOkonomi)) }
            ?: jsonInternalSoknad.withOkonomi(jsonOkonomi)
    }

    companion object Mapper {
        fun doKortMapping(
            okonomi: Okonomi,
            json: JsonOkonomi,
        ): JsonOkonomi = okonomi.setupKortMappers().fold(json) { accumulated, mapper -> mapper.doMapping(accumulated) }

        fun doMapping(
            okonomi: Okonomi,
            json: JsonOkonomi,
        ): JsonOkonomi = okonomi.setupMappers().fold(json) { accumulated, mapper -> mapper.doMapping(accumulated) }
    }
}

private fun Okonomi.setupMappers(): List<OkonomiElementsToJsonMapper> =
    listOf(
        FormueToJsonMapper(formuer),
        InntektToJsonMapper(inntekter, bekreftelser),
        UtgiftToJsonMapper(utgifter),
        BostotteSakToJsonMapper(bostotteSaker),
    ).let { list ->
        when {
            bekreftelser.isEmpty() -> list
            else -> list.plus(BekreftelseToJsonMapper(bekreftelser))
        }
    }

private fun Okonomi.setupKortMappers(): List<OkonomiElementsToJsonMapper> =
    listOf(
        InntektToJsonMapper(inntekter, bekreftelser),
        BostotteSakToJsonMapper(bostotteSaker),
        FormueToJsonMapper(formuer),
    ).let { list ->
        when {
            bekreftelser.isEmpty() -> list
            else -> list.plus(BekreftelseToJsonMapper(bekreftelser))
        }
    }

// JsonOpplysninger og JsonOversikt er required i JsonOkonomi selv uten data
private fun JsonInternalSoknad.initializeObjects(): JsonOkonomi =
    soknad!!.data.okonomi ?: JsonOkonomi(
        opplysninger = JsonOkonomiopplysninger(emptyList(), emptyList(), initBeskrivelser(), emptyList(), null),
        oversikt = JsonOkonomioversikt(emptyList(), emptyList(), emptyList()),
    )

private fun JsonInternalSoknad.withOkonomi(okonomi: JsonOkonomi): JsonInternalSoknad =
    soknad!!.let { soknad -> copy(soknad = soknad.copy(data = soknad.data.copy(okonomi = okonomi))) }

fun initBeskrivelser(): JsonOkonomibeskrivelserAvAnnet =
    JsonOkonomibeskrivelserAvAnnet(JsonKildeBruker.BRUKER, "", "", "", "", "")
