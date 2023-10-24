package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.domene.personalia.PersonForSoknad
import no.nav.sosialhjelp.soknad.domene.personalia.PersonForSoknadId
import no.nav.sosialhjelp.soknad.domene.personalia.repository.PersonRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException

class PersonForSoknadRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Test
    fun `Lagre Person`() {
        val soknad = opprettSoknad()

        val person = PersonForSoknad(
            id = PersonForSoknadId(
                personId = "1234561234",
                soknadId = soknad.id
            ),
            fornavn = "Test",
            etternavn = "Testesen"
        )

        val lagretPerson = personRepository.save(person)
        assertThat(lagretPerson).isEqualTo(person)
    }

    @Test
    fun `Person allerede lagt til kan ikke oppdateres`() {
        val soknad = opprettSoknad()

        val person = PersonForSoknad(
            id = PersonForSoknadId(
                personId = "1234561234",
                soknadId = soknad.id
            ),
            fornavn = "Test",
            etternavn = "Testesen"
        )

        val lagretPerson = personRepository.save(person)
        assertThat(lagretPerson).isEqualTo(person)

        assertThatThrownBy { personRepository.save(person) }
            .isInstanceOf(DuplicateKeyException::class.java)
    }

    @Test
    fun `Alle personer tilknyttet soknad forsvinner nar soknad slettes`() {
        val soknad = opprettSoknad()

        val eier =
            PersonForSoknad(PersonForSoknadId("23456712345", soknad.id), "Eier", etternavn = "Eiersen")
                .also { personRepository.save(it) }
        val barn =
            PersonForSoknad(PersonForSoknadId("54234264234", soknad.id), "Barn", etternavn = "Barnesen")
                .also { personRepository.save(it) }
        val ektefelle =
            PersonForSoknad(PersonForSoknadId("12345612345", soknad.id), "Kone", etternavn = "Konesen")
                .also { personRepository.save(it) }

        assertThat(personRepository.existsById(eier.id)).isTrue()
        assertThat(personRepository.existsById(barn.id)).isTrue()
        assertThat(personRepository.existsById(ektefelle.id)).isTrue()

        soknadRepository.delete(soknad)

        assertThat(personRepository.existsById(eier.id)).isFalse()
        assertThat(personRepository.existsById(barn.id)).isFalse()
        assertThat(personRepository.existsById(ektefelle.id)).isFalse()
    }
}
