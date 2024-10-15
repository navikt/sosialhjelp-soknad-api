package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.situasjonendring.JsonSituasjonendring
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.Situasjonsendring
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SituasjonsendringToJsonMapper(
    private val situasjonsendringRepository: SituasjonsendringRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        // No-op
    }

    override fun mapToKortJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        situasjonsendringRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
    }

    internal companion object Mapper {
        fun doMapping(
            situasjonsendring: Situasjonsendring,
            json: JsonInternalSoknad,
        ) {
            json.initializeObjects()
            json.soknad.data.situasjonendring =
                JsonSituasjonendring()
                    .withHarNoeEndretSeg(situasjonsendring.endring ?: false)
                    .withHvaHarEndretSeg(situasjonsendring.hvaErEndret)
                    .withKilde(JsonKildeBruker.BRUKER)
        }
    }
}

private fun JsonInternalSoknad.initializeObjects() {
    if (soknad.data == null) {
        soknad.withData(JsonData())
    }
}
