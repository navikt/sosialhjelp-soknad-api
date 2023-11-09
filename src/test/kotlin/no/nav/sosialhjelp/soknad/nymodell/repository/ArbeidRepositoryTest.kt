//package no.nav.sosialhjelp.soknad.nymodell.repository
//
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeid
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.repository.ArbeidRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Stillingstype
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThatThrownBy
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.relational.core.conversion.DbActionExecutionException
//import java.util.*
//
//class ArbeidRepositoryTest: RepositoryTest() {
//
//    @Autowired
//    private lateinit var arbeidRepository: ArbeidRepository
//
//    @Test
//    fun `Lagre nytt arbeid-objekt`() {
//        val soknad = opprettSoknad()
//        arbeidRepository.save(
//            Arbeid(
//                id = soknad.soknadId,
//                kommentarArbeid = "kommentar",
//                arbeidsforhold = opprettFlereArbeidsforhold(soknad.soknadId)
//            )
//        )
//        val arbeid = arbeidRepository.findById(soknad.soknadId).get()
//        assertThat(arbeid.arbeidsforhold.size).isEqualTo(4)
//    }
//
//    @Test
//    fun `Kan ikke opprette Arbeid uten Soknad`() {
//        assertThatThrownBy { arbeidRepository.save(Arbeid(UUID.randomUUID())) }
//            .isInstanceOf(DbActionExecutionException::class.java)
//    }
//
//    @Test
//    fun `Test oppdatere Arbeid`() {
//        val soknad = opprettSoknad()
//        val arbeid = opprettArbeid(soknad.soknadId)
//        arbeid.kommentarArbeid = "kommentar"
//        arbeidRepository.save(arbeid)
//
//        val updatedArbeid = arbeidRepository.findById(arbeid.id).get()
//        assertThat(updatedArbeid.kommentarArbeid).isEqualTo("kommentar")
//    }
//
//    @Test
//    fun `Slette Arbeid`() {
//        val soknad = opprettSoknad()
//        val arbeid = opprettArbeid(soknad.soknadId)
//
//        arbeidRepository.delete(arbeid)
//        assertThat(arbeidRepository.existsById(arbeid.id)).isFalse()
//    }
//
//    @Test
//    fun `Slette Soknad skal slette Arbeid`() {
//        val soknad = opprettSoknad()
//        val arbeid = opprettArbeid(soknad.soknadId)
//
//        soknadRepository.delete(soknad)
//        assertThat(arbeidRepository.existsById(arbeid.id)).isFalse()
//
//        val numberOfRows = jdbcTemplate
//            .queryForObject("select count(*) from arbeidsforhold", Int::class.java) as Int
//        assertThat(numberOfRows).isEqualTo(0)
//    }
//
//    private fun opprettArbeid(soknadId: UUID): Arbeid {
//        return arbeidRepository.save(
//            Arbeid(
//                id = soknadId,
//                kommentarArbeid = null,
//                arbeidsforhold = opprettFlereArbeidsforhold(soknadId)
//            )
//        )
//    }
//
//    private fun opprettFlereArbeidsforhold(arbeidId: UUID): Set<Arbeidsforhold> {
//        val arbeidsforholdSet: MutableSet<Arbeidsforhold> = mutableSetOf()
//        for(i in 0..3) {
//            arbeidsforholdSet.add(opprettArbeidsforhold(arbeidId, "Test-firma$i"))
//        }
//        return arbeidsforholdSet
//    }
//    private fun opprettArbeidsforhold(arbeidId: UUID, navn: String = "Test-firma"): Arbeidsforhold {
//        return Arbeidsforhold(
//            soknadId = arbeidId,
//            arbeidsgivernavn = navn,
//            stillingstype = Stillingstype.FAST
//        )
//    }
//}