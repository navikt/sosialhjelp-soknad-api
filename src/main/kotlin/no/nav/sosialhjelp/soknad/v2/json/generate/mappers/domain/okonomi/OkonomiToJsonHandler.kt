package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.toUTCTimestampStringWithMillis
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
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
            okonomi.handleBostotteSpecialCase(json)
        }
    }
}

private fun Okonomi.setupMappers(json: JsonOkonomi): List<OkonomiElementsToJsonMapper> {
    return listOf(
        FormueToJsonMapper(formuer, json),
        InntektToJsonMapper(inntekter, json),
        UtgiftToJsonMapper(utgifter, json),
    )
        .let { list ->
            if (bekreftelser.isNotEmpty()) {
                list.plus(BekreftelseToJsonMapper(bekreftelser, json))
            } else {
                list
            }
        }
        .let { list ->
            if (bostotteSaker.isNotEmpty()) {
                list.plus(BostotteSakToJsonMapper(bostotteSaker, json))
            } else {
                list
            }
        }
}

// Det skal være innslag av Utbetaling Husbanken hvis bostotte == true && samtykke == false
private fun Okonomi.handleBostotteSpecialCase(json: JsonOkonomi) {
    val bostotte = bekreftelser.find { it.type == BekreftelseType.BOSTOTTE } ?: return
    if (bostotte.verdi) {
        bekreftelser
            .find { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE }?.verdi
            ?.also { hasSamtykke -> if (!hasSamtykke) json.addUtbetalingHusbankenKildeBruker(bostotte.tidspunkt) }
    }
}

private fun JsonOkonomi.addUtbetalingHusbankenKildeBruker(tidspunkt: LocalDateTime) {
    opplysninger.utbetaling.add(
        JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.BRUKER)
            .withTittel(BekreftelseType.BOSTOTTE.toTittel())
            .withType(InntektType.UTBETALING_HUSBANKEN.name)
            .withUtbetalingsdato(tidspunkt.toUTCTimestampStringWithMillis())
            // TODO Hva betyr egentlig denne ?
            .withOverstyrtAvBruker(false),
    )
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
}
