package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueRepository
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import org.springframework.stereotype.Component
import java.util.*

@Component
class FormueMapper(private val formueRepository: FormueRepository): DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad) {
        json.createChildrenIfNotExists()
        json.soknad.data.okonomi.oversikt
            .formue
            .addAll(formueRepository.findAllBySoknadId(soknadId)
                .map { it.toJsonOkonomioversiktFormue() })
    }

    private fun Formue.toJsonOkonomioversiktFormue(): JsonOkonomioversiktFormue {
        return JsonOkonomioversiktFormue()
            .withKilde(JsonKilde.BRUKER) // alltid bruker
            .withType(type.toSoknadJsonType())
            .withTittel(tittel)
            .withBelop(belop)
    }
}