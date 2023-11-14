package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bostotte
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BostotteRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.DomainToJsonMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class BostotteMapper(
    private val bostotteRepository: BostotteRepository
): DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad) {
        json.createChildrenIfNotExists()
        with(json.soknad.data.okonomi.opplysninger.bostotte) {
            val jsonBostotteSakList = bostotteRepository.findAllBySoknadId(soknadId).map { it.toJsonBostotteSak() }
            saker.addAll(jsonBostotteSakList)
        }
    }

    private fun Bostotte.toJsonBostotteSak(): JsonBostotteSak {
        return JsonBostotteSak()
            .withType(type)
            .withDato(dato.toString())
            .withStatus(status?.name)
            .withBeskrivelse(beskrivelse)
            .withVedtaksstatus(vedtaksstatus?.toJsonBostotteSak_Vedtaksstatus())
    }

    private fun Vedtaksstatus.toJsonBostotteSak_Vedtaksstatus(): JsonBostotteSak.Vedtaksstatus {
        return when (this) {
            Vedtaksstatus.INNVILGET -> JsonBostotteSak.Vedtaksstatus.INNVILGET
            Vedtaksstatus.AVSLAG -> JsonBostotteSak.Vedtaksstatus.AVSLAG
            Vedtaksstatus.AVVIST -> JsonBostotteSak.Vedtaksstatus.AVVIST
        }
    }
}
