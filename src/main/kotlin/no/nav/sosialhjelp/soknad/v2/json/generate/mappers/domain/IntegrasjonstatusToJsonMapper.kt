package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.soknad.Integrasjonstatus
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IntegrasjonstatusToJsonMapper(
    private val integrasjonstatusRepository: IntegrasjonstatusRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad {
        val integrasjonstatus = integrasjonstatusRepository.findByIdOrNull(soknadId) ?: Integrasjonstatus(soknadId)

        return doMapping(
            integrasjonstatus = integrasjonstatus,
            json = jsonInternalSoknad,
        )
    }

    internal companion object Mapper {
        fun doMapping(
            integrasjonstatus: Integrasjonstatus,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad {
            val soknad = requireNotNull(json.soknad)
            return json.copy(soknad = soknad.copy(driftsinformasjon = integrasjonstatus.toJsonDriftsinformasjon()))
        }

        private fun Integrasjonstatus.toJsonDriftsinformasjon(): JsonDriftsinformasjon =
            JsonDriftsinformasjon(feilInntektSkatteetaten, feilUtbetalingerNav, feilStotteHusbanken)
    }
}
