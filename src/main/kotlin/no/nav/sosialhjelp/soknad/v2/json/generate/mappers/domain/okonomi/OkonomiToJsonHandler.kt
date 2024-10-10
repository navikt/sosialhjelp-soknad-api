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
    fun doMapping()
}

@Component
class OkonomiToJsonHandler(
    private val okonomiRepository: OkonomiRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        val jsonOkonomi = jsonInternalSoknad.initializeObjects()

        okonomiRepository.findByIdOrNull(soknadId)?.let { okonomi ->
            doMapping(okonomi, jsonOkonomi)
        }
    }

    companion object Mapper {
        fun doMapping(
            okonomi: Okonomi,
            json: JsonOkonomi,
        ) {
            okonomi.setupMappers(json).forEach { mapper -> mapper.doMapping() }
        }
    }
}

private fun Okonomi.setupMappers(json: JsonOkonomi): List<OkonomiElementsToJsonMapper> {
    return listOf(
        FormueToJsonMapper(formuer, json),
        InntektToJsonMapper(inntekter, json, bekreftelser),
        UtgiftToJsonMapper(utgifter, json),
        BostotteSakToJsonMapper(bostotteSaker, json),
    )
        .let { list ->
            when {
                bekreftelser.isEmpty() -> list
                else -> list.plus(BekreftelseToJsonMapper(bekreftelser, json))
            }
        }
}

// JsonOpplysninger og JsonOversikt er required i JsonOkonomi selv uten data
private fun JsonInternalSoknad.initializeObjects(): JsonOkonomi {
    val jsonOkonomi = soknad.data.okonomi ?: soknad.data.withOkonomi(JsonOkonomi()).okonomi
    return jsonOkonomi.apply {
        oversikt ?: withOversikt(JsonOkonomioversikt())
        opplysninger ?: withOpplysninger(JsonOkonomiopplysninger())
    }
}

fun JsonOkonomiopplysninger.initJsonBeskrivelser(): JsonOkonomibeskrivelserAvAnnet {
    return JsonOkonomibeskrivelserAvAnnet().apply {
        sparing = ""
        verdi = ""
        utbetaling = ""
        barneutgifter = ""
        boutgifter = ""
    }
        .also { withBeskrivelseAvAnnet(it) }
        .withKilde(JsonKildeBruker.BRUKER)
}
