package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.data.repository.ListCrudRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test", "test-container")
abstract class AbstractGenericRepositoryTest {

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository
    @Autowired
    protected lateinit var livssituasjonRepository: LivssituasjonRepository
    @Autowired
    protected lateinit var eierRepository: EierRepository
    @Autowired
    protected lateinit var kontaktRepository: KontaktRepository
    @Autowired
    protected lateinit var familieRepository: FamilieRepository

    protected lateinit var soknad: Soknad

    @BeforeEach
    fun saveSoknad() {
        soknad = soknadRepository.save(
            opprettSoknad(id = UUID.randomUUID())
        )
    }

    /**
     * Merk at dette er en extension-funksjon for typer av UpsertRepository og ListCrudRepository
     */
    protected fun <E : SoknadBubble, R> R.verifyCRUDOperations(
        originalEntity: E,
        updatedEntity: E,
    ) where R : UpsertRepository<E>, R : ListCrudRepository<E, UUID> {

        assertThat(originalEntity.soknadId).isEqualTo(updatedEntity.soknadId)
        assertThat(originalEntity).isNotEqualTo(updatedEntity)

        // lagre entitet
        val savedOriginalEntity = save(originalEntity)
        assertThat(existsById(originalEntity.soknadId)).isTrue()

        // oppdatere entitet
        val savedUpdatedEntity = save(updatedEntity)
        assertThat(savedUpdatedEntity).isNotEqualTo(savedOriginalEntity)

        // slette soknad-entiteten skal også slette denne entiteten
        soknadRepository.deleteById(originalEntity.soknadId)
        assertThat(this.existsById(originalEntity.soknadId)).isFalse()

        // lagre en entitet uten eksisterende soknad-referanse skal feile
        assertThatThrownBy { save(originalEntity) }
            .isInstanceOf(DbActionExecutionException::class.java)
    }
}
