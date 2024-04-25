package no.nav.sosialhjelp.soknad.v2.config.repository

import java.util.UUID
import org.springframework.data.jdbc.core.JdbcAggregateTemplate

interface DomainRoot {
    val soknadId: UUID
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
                existsById(s.soknadId, s.javaClass) -> update(s)
                else -> insert(s)
            }
        }
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> = entities.map { save(it) }
}
