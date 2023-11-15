package no.nav.sosialhjelp.soknad.nymodell.domene

import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

/**
 * Felles-wrapper for Navn.
 * @see [no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Eier]
 * @See [no.nav.sosialhjelp.soknad.nymodell.domene.familie.Barn]
 * @See [no.nav.sosialhjelp.soknad.nymodell.domene.familie.Ektefelle]
 */
data class Navn(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
)

/**
 * Interface som brukes som base for alle SoknadBubble(s)-objekter.
 * Gjør id tilgjengelig for det generiske UpsertRepository for valget insert/update
 */
interface HasUuidAsId {
    val id: UUID
}
/**
 * En Søknad-boble - en boble har et semantisk "en til en"-forhold med en Soknad, hvorav soknadens id også er boblens id
 */
abstract class SoknadBubble (
    open val soknadId: UUID,
): HasUuidAsId { override val id: UUID get() = soknadId }

/**
 * Soknad-bobler - disse er semantisk knyttet direkte til en soknad med et "en til mange"-forhold
 */
abstract class SoknadBubbles (
    override val id: UUID = UUID.randomUUID(),
    open val soknadId: UUID
): HasUuidAsId

/**
 * Felles Repository-Interface for SoknadBubbles. Tilgjengeliggjør en metode for å finne alle objekter (av typen)
 * basert på soknad-id.
 */
@NoRepositoryBean
interface BubblesRepository<T: SoknadBubbles>: ListCrudRepository<T, UUID> {
    fun findAllBySoknadId(soknadId: UUID): List<T>
}

/**
 * Et marker-interface for Repositories for objekter med semantisk "en til en"-forhold.
 */
@NoRepositoryBean
interface BubbleRepository<T: SoknadBubble>: ListCrudRepository<T, UUID>

enum class Kilde { BRUKER, SYSTEM; }
