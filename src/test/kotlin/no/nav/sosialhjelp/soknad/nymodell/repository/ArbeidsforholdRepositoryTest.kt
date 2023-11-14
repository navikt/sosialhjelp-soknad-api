package no.nav.sosialhjelp.soknad.nymodell.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.ArbeidsforholdRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Stillingstype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import java.util.*

class ArbeidsforholdRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var arbeidsforholdRepository: ArbeidsforholdRepository

    @Test
    fun `Lagre nytt arbeid-objekt`() {
        val soknad = opprettSoknad()
        arbeidsforholdRepository.save(
            Arbeidsforhold(
                soknadId = soknad.id,
                arbeidsgivernavn = "Arbeidsgivernsen"
            )
        )
        val arbeidsforhold = arbeidsforholdRepository.findAllBySoknadId(soknad.id)
        assertThat(arbeidsforhold.size).isEqualTo(1)
    }

    @Test
    fun `Kan ikke opprette Arbeid uten Soknad`() {
        assertThatThrownBy { arbeidsforholdRepository.save(Arbeidsforhold(soknadId = UUID.randomUUID())) }
            .isInstanceOf(DbActionExecutionException::class.java)
    }

    @Test
    fun `Test oppdatere Arbeid`() {
        val soknad = opprettSoknad()
        val arbeidsforhold = opprettArbeidsforhold(soknad.id)
        arbeidsforholdRepository.save(arbeidsforhold)

        arbeidsforholdRepository.findAllBySoknadId(soknad.id).let {
            assertThat(it).hasSize(1)
        }
    }

    @Test
    fun `Slette Arbeidsforhold`() {
        val soknad = opprettSoknad()
        val arbeidsforhold = opprettArbeidsforhold(soknad.id)

        assertThat(arbeidsforholdRepository.existsById(arbeidsforhold.id)).isTrue()
        arbeidsforholdRepository.delete(arbeidsforhold)
        assertThat(arbeidsforholdRepository.existsById(arbeidsforhold.id)).isFalse()
    }

    @Test
    fun `Slette Soknad skal slette Arbeid`() {
        val soknad = opprettSoknad()
        val arbeidsforhold = opprettArbeidsforhold(soknad.id)

        assertThat(arbeidsforholdRepository.existsById(arbeidsforhold.id)).isTrue()
        soknadRepository.delete(soknad)
        assertThat(arbeidsforholdRepository.existsById(arbeidsforhold.id)).isFalse()

        val numberOfRows = jdbcTemplate
            .queryForObject("select count(*) from arbeidsforhold", Int::class.java) as Int
        assertThat(numberOfRows).isEqualTo(0)
    }

    private fun opprettArbeidsforhold(soknadId: UUID): Arbeidsforhold {
        return arbeidsforholdRepository.save(
            Arbeidsforhold(
                soknadId = soknadId,
                arbeidsgivernavn = "Arbeidsgiversen"
            )
        )
    }

    private fun opprettFlereArbeidsforhold(arbeidId: UUID): Set<Arbeidsforhold> {
        val arbeidsforholdSet: MutableSet<Arbeidsforhold> = mutableSetOf()
        for(i in 0..3) {
            arbeidsforholdSet.add(opprettArbeidsforhold(arbeidId, "Test-firma$i"))
        }
        return arbeidsforholdSet
    }
    private fun opprettArbeidsforhold(arbeidId: UUID, navn: String = "Test-firma"): Arbeidsforhold {
        return Arbeidsforhold(
            soknadId = arbeidId,
            arbeidsgivernavn = navn,
            stillingstype = Stillingstype.FAST
        )
    }
}