package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.ArbeidsforholdRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.BosituasjonRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Stillingstype
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Utdanning
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.UtdanningRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class LivssituasjonMapper(
    private val bosituasjonRepository: BosituasjonRepository,
    private val utdanningRepository: UtdanningRepository,
    private val arbeidsforholdRepository: ArbeidsforholdRepository
): DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad) {
        val bosituasjon = bosituasjonRepository.findByIdOrNull(soknadId)
        val utdanning = utdanningRepository.findByIdOrNull(soknadId)
        val arbeidsforhold = arbeidsforholdRepository.findAllBySoknadId(soknadId)

        json.soknad.data
            .withBosituasjon(bosituasjon?.toJsonBosituasjon())
            .withUtdanning(utdanning?.toJsonUtdanning())

        mapArbeidsforhold(arbeidsforhold, json)
    }

    private fun mapArbeidsforhold(arbeidsforhold: List<Arbeidsforhold>, json: JsonInternalSoknad) {
        with(json.soknad.data) {
            if (arbeid == null) withArbeid(JsonArbeid())

            arbeid.withForhold(
                arbeidsforhold.map { it.toJsonArbeidsforhold() }
            )
        }
    }
}

fun Bosituasjon.toJsonBosituasjon(): JsonBosituasjon =
    JsonBosituasjon()
        .withBotype(JsonBosituasjon.Botype.valueOf(botype!!.name))
        .withAntallPersoner(antallPersoner)

fun Arbeidsforhold.toJsonArbeidsforhold() = JsonArbeidsforhold()
    .withKilde(JsonKilde.SYSTEM)
    .withArbeidsgivernavn(arbeidsgivernavn)
    .withFom(fraOgMed)
    .withTom(tilOgMed)
    .withStillingsprosent(stillingsprosent)
    .withStillingstype(stillingstype?.toStillingstype())

fun Utdanning.toJsonUtdanning() = JsonUtdanning()
    .withKilde(JsonKilde.BRUKER)
    .withErStudent(erStudent)
    .withStudentgrad(studentGrad?.toStudentgrad())

fun Studentgrad.toStudentgrad() = JsonUtdanning.Studentgrad.valueOf(name)

fun Stillingstype.toStillingstype() = JsonArbeidsforhold.Stillingstype.valueOf(name)

