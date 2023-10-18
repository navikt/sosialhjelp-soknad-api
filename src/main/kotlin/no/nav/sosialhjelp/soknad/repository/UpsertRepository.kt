package no.nav.sosialhjelp.soknad.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate


// Samme signatur som CrudRepository slik at denne overrides når et
// repository-interface implementerer begge
interface UpsertRepository<T: DelAvSoknad> {
    fun <S : T> save(s: S): S
}
class UpsertRepositoryImpl<T: DelAvSoknad>(
    private val jdbcAggregateTemplate: JdbcAggregateTemplate
): UpsertRepository<T> {
    override fun <S : T> save(s: S): S {
        return if (jdbcAggregateTemplate.existsById(s.id, s.javaClass)) {
            jdbcAggregateTemplate.update(s)
        } else {
            jdbcAggregateTemplate.insert(s)
        }
    }
}
