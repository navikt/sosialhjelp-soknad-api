//package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
//import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
//import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Utdanning
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.repository.BosituasjonRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.repository.UtdanningRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Studentgrad
//import org.springframework.data.repository.findByIdOrNull
//import org.springframework.stereotype.Component
//import java.util.*
//
//@Component
//class LivssituasjonMapper(
//    private val bosituasjonRepository: BosituasjonRepository,
//    private val utdanningRepository: UtdanningRepository
//): DomainToJsonMapper {
//    override fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
//        val bosituasjon = bosituasjonRepository.findByIdOrNull(soknadId)
//        val utdanning = utdanningRepository.findByIdOrNull(soknadId)
//
//        jsonInternalSoknad.soknad.data
//            .withBosituasjon(bosituasjon?.toJsonBosituasjon())
//            .withUtdanning(utdanning?.toJsonUtdanning())
//    }
//}
//
//fun Bosituasjon.toJsonBosituasjon(): JsonBosituasjon =
//    JsonBosituasjon()
//        .withBotype(JsonBosituasjon.Botype.valueOf(botype!!.name))
//        .withAntallPersoner(antallPersoner)
//
//fun Arbeid.toJsonArbeid(): JsonArbeid = JsonArbeid()
//    .withKommentarTilArbeidsforhold(toJsonKommentarTilArbeidsforhold())
//    .withForhold( arbeidsforhold.map { it.toJsonArbeidsforhold() } )
//
//fun Arbeid.toJsonKommentarTilArbeidsforhold() =
//    kommentarArbeid?.let { JsonKommentarTilArbeidsforhold().withVerdi(it) }
//
//fun Arbeidsforhold.toJsonArbeidsforhold() = JsonArbeidsforhold()
//    .withKilde(JsonKilde.SYSTEM)
//    .withArbeidsgivernavn(arbeidsgivernavn)
//    .withFom(fraOgMed)
//    .withTom(tilOgMed)
//    .withStillingsprosent(stillingsprosent)
//    .withStillingstype(JsonArbeidsforhold.Stillingstype.valueOf(stillingstype.name))
//
//fun Utdanning.toJsonUtdanning() = JsonUtdanning()
//    .withKilde(JsonKilde.BRUKER)
//    .withErStudent(erStudent)
//    .withStudentgrad(studentGrad.toStudentgrad())
//
//fun Studentgrad.toStudentgrad() = JsonUtdanning.Studentgrad.valueOf(name)
//
