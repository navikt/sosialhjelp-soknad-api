package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Order(Ordered.HIGHEST_PRECEDENCE) // Sørger for at denne mapperen er den første som kjører
@Component
class SoknadToJsonMapper(
    private val soknadRepository: SoknadRepository
) : DomainToJsonMapper {
    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {

        soknadRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
            ?: throw IkkeFunnetException("Soknad finnes ikke")
    }

    internal companion object Mapper {
        fun doMapping(domainSoknad: Soknad, json: JsonInternalSoknad) {
            with(json) {
                initializeObjects()

                soknad.innsendingstidspunkt = domainSoknad.tidspunkt.sendtInn
                    ?.let { OffsetDateTime.of(it, ZoneOffset.UTC).toString() }

                domainSoknad.begrunnelse?.let {
                    soknad.data.begrunnelse = it.toJsonBegrunnelse()
                }
            }
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            // required i json-modellen (validering)
            soknad.data.begrunnelse ?: soknad.data.withBegrunnelse(JsonBegrunnelse())
        }

        private fun Begrunnelse.toJsonBegrunnelse(): JsonBegrunnelse {
            return JsonBegrunnelse()
                .withHvaSokesOm(hvaSokesOm)
                .withHvorforSoke(hvorforSoke)
        }
    }
}
