package no.nav.sosialhjelp.soknad.nymodell.domene

import org.springframework.data.jdbc.core.JdbcAggregateTemplate

/**
 * Felles fragment interface for alle objekter hvor UUID er @Id
 *
 * Samme signatur som CrudRepository#save slik at denne overrides når et
 * repository-interface implementerer begge.
 */
interface UpsertRepository<T: HasUuidAsId> {
    fun <S : T> save(s: S): S
    fun <S : T> saveAll(entities: Iterable<S>): List<S>
}
class UpsertRepositoryImpl<T: HasUuidAsId>(
    private val jdbcAggregateTemplate: JdbcAggregateTemplate
): UpsertRepository<T> {
    override fun <S : T> save(s: S): S {
        return if (jdbcAggregateTemplate.existsById(s.id, s.javaClass)) {
            jdbcAggregateTemplate.update(s)
        } else {
            jdbcAggregateTemplate.insert(s)
        }
    }
    override fun <S : T> saveAll(entities: Iterable<S>): List<S> = entities.map { save(it) }
}
