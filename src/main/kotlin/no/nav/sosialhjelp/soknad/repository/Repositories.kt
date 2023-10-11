package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.model.Fil
import no.nav.sosialhjelp.soknad.model.Soknad
import no.nav.sosialhjelp.soknad.model.Vedlegg
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

// eksempel på hvordan lage generelle repositories-metoder
@NoRepositoryBean
interface CommonRepository<T: PartOfSoknad> : ListCrudRepository<T, UUID> {
    @Query("SELECT * FROM soknad where id = :soknadId")
    fun findSoknad(soknadId: UUID): Soknad
}

@Repository
interface SoknadRepository: UpsertRepository<Soknad>, CommonRepository<Soknad>
@Repository
interface BosituasjonRepository : UpsertRepository<Bosituasjon>, CommonRepository<Bosituasjon>
@Repository
interface VedleggRepository : ListCrudRepository<Vedlegg, Long> {
    fun findAllBySoknadId(soknadId: UUID): List<Vedlegg>
}
@Repository
interface FilRepository : ListCrudRepository<Fil, Long> {
    fun findAllByVedleggId(vedleggId: Long): List<Fil>
}

interface UpsertRepository<T: PartOfSoknad> {
    fun <S : T> save(s: S): S
}
class UpsertRepositoryImpl<T: PartOfSoknad>(
    private val jdbcAggregateTemplate: JdbcAggregateTemplate
): UpsertRepository<T> {

    // samme signatur som default-implementasjonen for å erstatte denne med en "upsert"
    override fun <S : T> save(s: S): S {
        return if (jdbcAggregateTemplate.existsById(s.id, s.javaClass)) {
            jdbcAggregateTemplate.update(s)
        } else {
            jdbcAggregateTemplate.insert(s)
        }
    }
}

interface PartOfSoknad {
    val id: UUID
}
