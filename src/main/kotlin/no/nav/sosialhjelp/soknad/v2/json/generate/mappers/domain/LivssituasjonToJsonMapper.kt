package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker

@Component
class LivssituasjonToJsonMapper(
    private val livssituasjonRepository: LivssituasjonRepository,
) : DomainToJsonMapper {
    override fun mapToSoknad(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        livssituasjonRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
    }

    internal companion object Mapper {
        fun doMapping(
            livssituasjon: Livssituasjon,
            json: JsonInternalSoknad,
        ) {
            // noen felter forventes i validering
            json.initializeObjects()

            with(json.soknad.data) {
                livssituasjon.arbeid.let { this.arbeid = it.toJsonArbeid() }
                livssituasjon.utdanning?.let { this.utdanning = it.toJsonUtdanning() }
                livssituasjon.bosituasjon?.let { this.bosituasjon = it.toJsonBosituasjon() }
            }
        }
    }
}

// Disse er `required` i filformatet å må eksistere (hvis det skal validere)
private fun JsonInternalSoknad.initializeObjects() {
    soknad.data ?: soknad.withData(JsonData())
    with(soknad.data) {
        arbeid ?: withArbeid(JsonArbeid())
        utdanning ?: withUtdanning(JsonUtdanning())
        bosituasjon ?: withBosituasjon(JsonBosituasjon())
    }
}

private fun Arbeid.toJsonArbeid(): JsonArbeid {
    return JsonArbeid()
        .withKommentarTilArbeidsforhold(
            kommentar?.let {
                JsonKommentarTilArbeidsforhold()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(kommentar)
            }
        )
        .withForhold(arbeidsforhold.map { it.toJsonArbeidsforhold() })
}

private fun Utdanning.toJsonUtdanning(): JsonUtdanning? {
    return erStudent?.let {
        JsonUtdanning()
            .withKilde(JsonKilde.BRUKER)
            .withErStudent(it)
            .withStudentgrad(if (!it) null else studentgrad?.toJsonStudentgrad())
    }
}

private fun Studentgrad.toJsonStudentgrad() = JsonUtdanning.Studentgrad.fromValue(name.lowercase())

private fun Bosituasjon.toJsonBosituasjon(): JsonBosituasjon? {
    return if (botype == null && antallHusstand == null) {
        null
    } else {
        JsonBosituasjon()
            .withBotype(botype?.toJsonBotype())
            .withAntallPersoner(antallHusstand)
    }
}

private fun Botype.toJsonBotype() = JsonBosituasjon.Botype.fromValue(name.lowercase())

private fun Arbeidsforhold.toJsonArbeidsforhold(): JsonArbeidsforhold {
    return JsonArbeidsforhold()
        .withKilde(JsonKilde.SYSTEM)
        .withArbeidsgivernavn(arbeidsgivernavn)
        .withStillingstype(harFastStilling?.toJsonArbeidsforholdStillingtype())
        .withStillingsprosent(fastStillingsprosent)
        .withFom(start)
        .withTom(slutt)
        .withOverstyrtAvBruker(false)
}

private fun Boolean.toJsonArbeidsforholdStillingtype(): JsonArbeidsforhold.Stillingstype {
    return if (this) JsonArbeidsforhold.Stillingstype.FAST else JsonArbeidsforhold.Stillingstype.VARIABEL
}
