package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.toUTCTimestampStringWithMillis
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Order(Ordered.HIGHEST_PRECEDENCE) // Sørger for at denne mapperen er den første som kjører
@Component
class SoknadToJsonMapper(
    private val soknadRepository: SoknadRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        soknadRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
            ?: throw IkkeFunnetException("Soknad finnes ikke")
    }

    internal companion object Mapper {
        fun doMapping(
            domainSoknad: Soknad,
            json: JsonInternalSoknad,
        ) {
            with(json) {
                initializeObjects()

                soknad.data.personalia.personIdentifikator = domainSoknad.toJsonPersonIdentifikator()

                soknad.innsendingstidspunkt = domainSoknad.tidspunkt.sendtInn?.toUTCTimestampStringWithMillis()

                domainSoknad.begrunnelse.let {
                    soknad.data.begrunnelse = it.toJsonBegrunnelse()
                }
            }
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            soknad.data.personalia ?: soknad.data.withPersonalia(JsonPersonalia())

            // required i json-modellen (validering)
            soknad.data.begrunnelse ?: soknad.data.withBegrunnelse(JsonBegrunnelse())
        }

        private fun Soknad.toJsonPersonIdentifikator(): JsonPersonIdentifikator {
            return JsonPersonIdentifikator().withKilde(JsonPersonIdentifikator.Kilde.SYSTEM).withVerdi(eierPersonId)
        }

        private fun Begrunnelse.toJsonBegrunnelse(): JsonBegrunnelse =
            JsonBegrunnelse()
                .withHvaSokesOm(hvaSokesOm)
                .withHvorforSoke(hvorforSoke)
                .withKilde(JsonKildeBruker.BRUKER)
    }
}
