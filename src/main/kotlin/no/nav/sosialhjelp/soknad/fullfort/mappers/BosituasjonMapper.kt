package no.nav.sosialhjelp.soknad.fullfort.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sosialhjelp.soknad.fullfort.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.model.soknad.Bosituasjon
import no.nav.sosialhjelp.soknad.repository.BosituasjonRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class BosituasjonMapper(
    private val bosituasjonRepository: BosituasjonRepository
): SoknadToJsonMapper {
    override fun mapToSoknadJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val bosituasjon = bosituasjonRepository.findByIdOrNull(soknadId)
        bosituasjon?.let { jsonInternalSoknad.soknad.data.withBosituasjon(it.toJsonObject()) }
    }
}

fun Bosituasjon.toJsonObject(): JsonBosituasjon =
    JsonBosituasjon()
        .withBotype(JsonBosituasjon.Botype.valueOf(botype!!.name))
        .withAntallPersoner(antallPersoner)
