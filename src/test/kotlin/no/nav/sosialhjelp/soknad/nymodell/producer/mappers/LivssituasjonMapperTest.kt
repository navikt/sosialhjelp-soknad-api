package no.nav.sosialhjelp.soknad.nymodell.producer.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.ArbeidsforholdRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.BosituasjonRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Stillingstype
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Utdanning
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.UtdanningRepository
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.LivssituasjonMapper
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.*

@Import(LivssituasjonMapper::class)
class LivssituasjonMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var livssituasjonMapper: LivssituasjonMapper
    @Autowired
    private lateinit var bosituasjonRepository: BosituasjonRepository
    @Autowired
    private lateinit var utdanningRepository: UtdanningRepository
    @Autowired
    private lateinit var arbeidsforholdRepository: ArbeidsforholdRepository

    @Test
    fun `Map lagrede data til JsonInternalSoknad`() {
        val nySoknad = opprettSoknad()

        val bosituasjon: Bosituasjon = createAndSaveBosituasjon(nySoknad.id)
        val utdanning: Utdanning = createAndSaveUtdanning(nySoknad.id)
        val arbeidsforhold: List<Arbeidsforhold> = createAndSaveArbeidsforhold(nySoknad.id)

        val json = JsonInternalSoknad().apply { createChildrenIfNotExists() }

        livssituasjonMapper.mapDomainToJson(nySoknad.id, json)

        with (json) {
            soknad.data.arbeid.let {
                assertThat(it.forhold).hasSize(2)
                assertThat(it.forhold.find { it.arbeidsgivernavn == arbeidsforhold[0].arbeidsgivernavn }).isNotNull
                assertThat(it.forhold.find { it.arbeidsgivernavn == arbeidsforhold[1].arbeidsgivernavn }).isNotNull
            }

            soknad.data.utdanning.let {
                assertThat(it.erStudent).isEqualTo(utdanning.erStudent)
                assertThat(it.studentgrad.name).isEqualTo(utdanning.studentGrad?.name)
            }

            soknad.data.bosituasjon.let {
                assertThat(it.botype.name).isEqualTo(bosituasjon.botype?.name)
                assertThat(it.antallPersoner).isEqualTo(bosituasjon.antallPersoner)
            }
        }
    }

    private fun createAndSaveArbeidsforhold(soknadId: UUID): List<Arbeidsforhold> {
        return listOf(
            Arbeidsforhold(
                soknadId = soknadId,
                arbeidsgivernavn = "En arbeidsgiver",
                stillingsprosent = 100,
                stillingstype = Stillingstype.FAST
            ).also { arbeidsforholdRepository.save(it) },
            Arbeidsforhold(
                soknadId = soknadId,
                arbeidsgivernavn = "En annen arbeidsgiver",
                stillingsprosent = 50,
                stillingstype = Stillingstype.VARIABEL
            ).also { arbeidsforholdRepository.save(it) }
        )
    }

    private fun createAndSaveUtdanning(soknadId: UUID): Utdanning {
        return Utdanning(
            soknadId,
            erStudent = true,
            studentGrad = Studentgrad.HELTID
        ).also { utdanningRepository.save(it) }
    }

    private fun createAndSaveBosituasjon(soknadId: UUID): Bosituasjon {
        return Bosituasjon(
            soknadId,
            botype = Botype.EIER,
            antallPersoner = 4
        ).also { bosituasjonRepository.save(it) }
    }


}