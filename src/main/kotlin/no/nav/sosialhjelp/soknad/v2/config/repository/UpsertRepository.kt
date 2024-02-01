package no.nav.sosialhjelp.soknad.v2.config.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import java.util.*

/**
 * UpsertRepository er et fragment interface med egen implementasjon.
 * Overskriver signaturen til ListCrudRepository, slik at disse metodene erstatter default.
 */
interface UpsertRepository<T : SoknadBubble> {
    fun <S : T> save(s: S): S
    fun <S : T> saveAll(entities: Iterable<S>): List<S>
}

class UpsertRepositoryImpl<T : SoknadBubble>(
    private val template: JdbcAggregateTemplate
) : UpsertRepository<T> {
    override fun <S : T> save(s: S): S {
        return template.run {
            if (existsById(s.soknadId, s.javaClass)) update(s)
            else insert(s)
        }
    }
    override fun <S : T> saveAll(entities: Iterable<S>): List<S> = entities.map { save(it) }
}

interface SoknadBubble {
    val soknadId: UUID
}
