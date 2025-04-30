package no.nav.sosialhjelp.soknad.v2.config.repository

import org.postgresql.util.PSQLException
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
            when (template.existsById(s.getDbId(), s.javaClass)) {
                true -> template.update(s)
                false -> template.insert(s)
            }
        }
            .onFailure {
                generateErrorMessage(s, it)
                    .let { (errorString, throwable) -> throw RuntimeException(errorString, throwable) }
            }
            .getOrThrow()
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> = template.saveAll(entities).toList()
}

private fun generateErrorMessage(
    element: DomainRoot,
    throwable: Throwable,
): Pair<String, Throwable?> {
    val stringBuilder = StringBuilder()

    stringBuilder.append("Update failed for element: ${element.javaClass} with id: ${element.getDbId()}\n")
    stringBuilder.append("Caused By: ${throwable.javaClass}\n")

    var rootException: Throwable? = null

    // TODO dette bør være midlertidig...
    // vi opplever en feil hvor potensielt data fra bruker havner i console loggen - dette maskerer selve error message
    var currentCause: Throwable? = throwable
    while (currentCause != null) {
        if (currentCause is PSQLException) {
            rootException = currentCause
        } else {
            stringBuilder.append("Caused By: ${currentCause.javaClass}\n")
        }

        currentCause = currentCause.cause
    }
    return Pair(stringBuilder.toString(), rootException)
}
