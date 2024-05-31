package no.nav.sosialhjelp.soknad.v2.config.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import java.util.UUID

/**
 * Rot-objektet for et aggregat.
 * dbId vil være den unike id'en i databasen uavhengig av semantisk forhold 1-1 eller 1-n
 */
interface DomainRoot {
    fun getDbId(): UUID
}

/**
 * UpsertRepository er et fragment interface med egen implementasjon.
 * Overskriver signaturen til ListCrudRepository, slik at disse metodene erstatter default.
 */
interface UpsertRepository<T : DomainRoot> {
    fun <S : T> save(s: S): S

    fun <S : T> saveAll(entities: Iterable<S>): List<S>
}

class UpsertRepositoryImpl<T : DomainRoot>(
    private val template: JdbcAggregateTemplate,
) : UpsertRepository<T> {
    override fun <S : T> save(s: S): S {
        return template.run {
            when {
                existsById(s.getDbId(), s.javaClass) -> update(s)
                else -> insert(s)
            }
        }
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> = entities.map { save(it) }
}
