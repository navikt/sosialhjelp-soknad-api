package no.nav.sosialhjelp.soknad.nymodell.domene

import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.UuidAsIdObject
import org.springframework.data.jdbc.core.JdbcAggregateTemplate

// Samme signatur som CrudRepository.save slik at denne overrides når et
// repository-interface implementerer begge
// Hensikten er å velge riktig sql-spørring (insert/update) ved .save()
interface UpsertRepository<T: UuidAsIdObject> {
    fun <S : T> save(s: S): S
}
class UpsertRepositoryImpl<T: UuidAsIdObject>(
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
