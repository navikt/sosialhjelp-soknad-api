package no.nav.sosialhjelp.soknad.nymodell.producer.json

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.nymodell.producer.SoknadProducer
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.SoknadMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class JsonInternalSoknadProducer(
    private val soknadMapper: SoknadMapper
) : SoknadProducer<JsonInternalSoknad> {

    override fun produceNew(soknadId: UUID): JsonInternalSoknad {
        return produceFrom(
            soknadId = soknadId,
            obj = JsonInternalSoknad()
        )
    }

    override fun produceFrom(soknadId: UUID, obj: JsonInternalSoknad): JsonInternalSoknad {
        obj.createChildrenIfNotExists()
        mapToExistingJsonInternalSoknad(soknadId, obj)
        return obj
    }

    fun mapToExistingJsonInternalSoknad(soknadId: UUID, json: JsonInternalSoknad): JsonInternalSoknad {
        soknadMapper.mapSoknadToJson(soknadId, json)
        return json
    }
}

fun JsonInternalSoknad.createChildrenIfNotExists() {
    if (soknad == null) withSoknad(JsonSoknad())
    soknad.apply {
        if (data == null) soknad.withData(JsonData())
        data.apply {
            if (okonomi == null) withOkonomi(JsonOkonomi())
            okonomi.apply {
                if (opplysninger == null) opplysninger = JsonOkonomiopplysninger()
                if (oversikt == null) oversikt = JsonOkonomioversikt()
                opplysninger.apply {
                    if (bostotte == null) bostotte = JsonBostotte()
                    if (beskrivelseAvAnnet == null) beskrivelseAvAnnet = JsonOkonomibeskrivelserAvAnnet()
                }
            }
        }
    }
}
