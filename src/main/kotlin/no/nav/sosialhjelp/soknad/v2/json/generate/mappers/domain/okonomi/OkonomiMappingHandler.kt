package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

interface OkonomiDelegateMapper {
    fun doMapping()
}

@Component
class OkonomiMappingHandler(
    private val okonomiRepository: OkonomiRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        val jsonOkonomi = jsonInternalSoknad.initializeObjects()

        okonomiRepository.findByIdOrNull(soknadId)?.let {
            FormueToJsonMapper(it.formuer, jsonOkonomi).doMapping()
            InntektToJsonMapper(it.inntekter, jsonOkonomi)
            UtgiftToJsonMapper(it.utgifter, jsonOkonomi)
        }
    }
}

private fun JsonInternalSoknad.initializeObjects(): JsonOkonomi =
    soknad.data.let {
        it.okonomi ?: it.withOkonomi(JsonOkonomi()).okonomi
    }
