package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Integrasjonstatus
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class IntegrasjonstatusToJsonMapper(
    private val integrasjonstatusRepository: IntegrasjonstatusRepository
) : DomainToJsonMapper {
    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        doMapping(
            integrasjonstatus = integrasjonstatusRepository.findByIdOrNull(soknadId),
            jsonInternalSoknad
        )
    }

    internal companion object Mapper {

        fun doMapping(integrasjonstatus: Integrasjonstatus?, json: JsonInternalSoknad) {
            json.soknad.driftsinformasjon = integrasjonstatus?.toJsonDriftsinformasjon()
        }

        private fun Integrasjonstatus.toJsonDriftsinformasjon(): JsonDriftsinformasjon {
            return JsonDriftsinformasjon()
                .withUtbetalingerFraNavFeilet(feilUtbetalingerNav)
                .withInntektFraSkatteetatenFeilet(feilInntektSkatteetaten)
                .withStotteFraHusbankenFeilet(feilStotteHusbanken)
        }
    }
}
