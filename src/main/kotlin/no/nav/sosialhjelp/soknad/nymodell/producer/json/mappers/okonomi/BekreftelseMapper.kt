package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseRepository
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import org.springframework.stereotype.Component
import java.util.*

@Component
class BekreftelseMapper(
    private val bekreftelseRepository: BekreftelseRepository
) : DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad) {
        json.createChildrenIfNotExists()
        json.soknad.data.okonomi.opplysninger
            .bekreftelse
            .addAll(
                bekreftelseRepository.findAllBySoknadId(soknadId)
                    .map { it.toJsonOkonomibekreftelse() }
            )
    }

    private fun Bekreftelse.toJsonOkonomibekreftelse(): JsonOkonomibekreftelse {
        return JsonOkonomibekreftelse()
            .withKilde(JsonKilde.BRUKER) // alltid bruker
            .withType(type?.toSoknadJsonType())
            .withTittel(tittel)
            .withVerdi(bekreftet)
            .withBekreftelsesDato(dato.toString())
    }
}
