package no.nav.sosialhjelp.soknad.v2.config.repository

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
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
        return runCatching {
            template.run {
                when {
                    existsById(s.getDbId(), s.javaClass) -> update(s)
                    else -> insert(s)
                }
            }
        }
            .onFailure {
                logger.error(
                    "Feil ved databaseoperasjon: " + "JavaClass: ${s.javaClass} " +
                        "Cause: ${it.cause} " +
                        "Stack trace: ${it.stackTrace}",
                    it,
                )
            }
            .getOrElse {
                throw RuntimeException("Feil ved databaseoperasjon")
            }
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> = template.saveAll(entities).toList()

    companion object {
        private val logger by logger()
    }
}
