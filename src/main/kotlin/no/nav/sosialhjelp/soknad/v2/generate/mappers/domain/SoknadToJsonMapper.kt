package no.nav.sosialhjelp.soknad.v2.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Driftsinformasjon
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
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

                soknad.innsendingstidspunkt = domainSoknad.tidspunkt.sendtInn.toString()
                soknad.driftsinformasjon = domainSoknad.driftsinformasjon.toJsonDriftsinformasjon()
                soknad.data.begrunnelse = domainSoknad.begrunnelse.toJsonBegrunnelse()
            }
        }

        private fun JsonInternalSoknad.initializeObjects() {
            soknad.data ?: soknad.withData(JsonData())
            soknad.driftsinformasjon ?: soknad.withDriftsinformasjon(JsonDriftsinformasjon())
        }

        private fun Begrunnelse.toJsonBegrunnelse(): JsonBegrunnelse? {
            return if (hvaSokesOm == null && hvorforSoke == null) null
            else
                JsonBegrunnelse()
                    .withHvaSokesOm(hvaSokesOm)
                    .withHvorforSoke(hvorforSoke)
        }

        private fun Driftsinformasjon.toJsonDriftsinformasjon(): JsonDriftsinformasjon? {
            return JsonDriftsinformasjon()
                .withUtbetalingerFraNavFeilet(utbetalingerFraNav)
                .withInntektFraSkatteetatenFeilet(inntektFraSkatt)
                .withStotteFraHusbankenFeilet(stotteFraHusbanken)
        }
    }
}
