package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bostotte
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.repository.BekreftelseRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.repository.BostotteRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class OpplysningerAnnetMapper(
    private val bekreftelseRepository: BekreftelseRepository,
    private val bostotteRepository: BostotteRepository,
): DomainToJsonMapper {

    override fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val jsonOkonomi = jsonInternalSoknad.soknad.data.okonomi ?: JsonOkonomi()
        bekreftelseRepository.findAllBySoknadId(soknadId).also { jsonOkonomi.addBekreftelser(it) }
        bostotteRepository.findAllBySoknadId(soknadId).also { jsonOkonomi.addBostotteSaker(it) }
    }
}

fun JsonOkonomi.addBekreftelser(bekreftelser: List<Bekreftelse>) {
    bekreftelser.forEach { addBekreftelse(it) }
}

fun JsonOkonomi.addBekreftelse(bekreftelse: Bekreftelse) {
    initChildren()
    opplysninger.bekreftelse.add(bekreftelse.toJsonOkonomibekreftelse())
}

fun JsonOkonomi.addBostotteSaker(bostotteSaker: List<Bostotte>) {
    bostotteSaker.forEach { addBostotteSak(it) }
}

fun JsonOkonomi.addBostotteSak(bostotte: Bostotte) {
    initChildren()
    if (opplysninger.bostotte == null) opplysninger.withBostotte(JsonBostotte())
    opplysninger.bostotte.saker.add(bostotte.toJsonBostotteSak())
}
