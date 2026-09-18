package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Utdanning
import no.nav.sosialhjelp.soknad.v2.livssituasjon.toIsoString
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LivssituasjonToJsonMapper(
    private val livssituasjonRepository: LivssituasjonRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad = livssituasjonRepository.findByIdOrNull(soknadId)?.let { doMapping(it, jsonInternalSoknad) } ?: jsonInternalSoknad

    override fun mapToKortJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ): JsonInternalSoknad = livssituasjonRepository.findByIdOrNull(soknadId)?.let { doKortMapping(it, jsonInternalSoknad) } ?: jsonInternalSoknad

    internal companion object Mapper {
        fun doMapping(
            livssituasjon: Livssituasjon,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad {
            val soknad = requireNotNull(json.soknad)
            val data = requireNotNull(soknad.data)
            return json.copy(soknad = soknad.copy(data = data.copy(arbeid = livssituasjon.arbeid.toJsonArbeid(), utdanning = livssituasjon.utdanning?.toJsonUtdanning() ?: JsonUtdanning(JsonKilde.BRUKER), bosituasjon = livssituasjon.bosituasjon?.toJsonBosituasjon() ?: JsonBosituasjon(JsonKildeBruker.BRUKER))))
        }

        fun doKortMapping(
            livssituasjon: Livssituasjon,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad {
            val soknad = requireNotNull(json.soknad)
            val data = requireNotNull(soknad.data)
            return json.copy(soknad = soknad.copy(data = data.copy(arbeid = livssituasjon.arbeid.toJsonArbeid())))
        }
    }
}

private fun Arbeid.toJsonArbeid(): JsonArbeid =
    JsonArbeid(arbeidsforhold.map { it.toJsonArbeidsforhold() }, kommentarTilArbeidsforhold = kommentar?.let { JsonKommentarTilArbeidsforhold(JsonKildeBruker.BRUKER, it) })

private fun Utdanning.toJsonUtdanning(): JsonUtdanning? =
    erStudent.let {
        JsonUtdanning(JsonKilde.BRUKER, it, if (!it) null else studentgrad?.toJsonStudentgrad())
    }

private fun Studentgrad.toJsonStudentgrad() = JsonUtdanning.Studentgrad.fromValue(name.lowercase())

private fun Bosituasjon.toJsonBosituasjon(): JsonBosituasjon? =
    if (botype == null && antallHusstand == null) {
        null
    } else {
        JsonBosituasjon(JsonKildeBruker.BRUKER, botype?.toJsonBotype(), antallHusstand)
    }

private fun Botype.toJsonBotype() = JsonBosituasjon.Botype.fromValue(name.lowercase())

private fun Arbeidsforhold.toJsonArbeidsforhold(): JsonArbeidsforhold =
    JsonArbeidsforhold(JsonKilde.SYSTEM, arbeidsgivernavn ?: "", start?.toIsoString() ?: "", fastStillingsprosent?.toInt() ?: 0, false, slutt?.toIsoString(), harFastStilling?.toJsonArbeidsforholdStillingtype())

private fun Boolean.toJsonArbeidsforholdStillingtype(): JsonArbeidsforhold.Stillingstype = if (this) JsonArbeidsforhold.Stillingstype.FAST else JsonArbeidsforhold.Stillingstype.VARIABEL
