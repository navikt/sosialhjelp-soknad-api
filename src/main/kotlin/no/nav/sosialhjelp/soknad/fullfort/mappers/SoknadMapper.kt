package no.nav.sosialhjelp.soknad.fullfort.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sosialhjelp.soknad.fullfort.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.domene.soknad.Soknad
import no.nav.sosialhjelp.soknad.repository.SoknadRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class SoknadMapper (
    private val soknadRepository: SoknadRepository
): SoknadToJsonMapper {
    override fun mapToSoknadJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val soknad = soknadRepository.findById(soknadId).get()

        soknad.mapInnsendingstidspunktTo(jsonInternalSoknad)
        jsonInternalSoknad.soknad.data.withBegrunnelse(soknad.toJsonBegrunnelse())
    }
}

fun Soknad.toJsonBegrunnelse(): JsonBegrunnelse =
    JsonBegrunnelse()
        .withHvaSokesOm(hvaSokesOm)
        .withHvorforSoke(hvorforSoke)

fun Soknad.mapInnsendingstidspunktTo(jsonInternalSoknad: JsonInternalSoknad) {
    jsonInternalSoknad.soknad.withInnsendingstidspunkt(innsendingstidspunkt.toString())
}