package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.v2.config.repository.AggregateRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Repository
interface SoknadRepository : UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID>
fun SoknadRepository.findOrError(soknadId: UUID) = findByIdOrNull(soknadId) ?: error("Kunne ikke finne soknad: $soknadId")

data class Soknad(
    @Id
    val id: UUID = UUID.randomUUID(),
    val eierPersonId: String,
    @Embedded.Empty
    val tidspunkt: Tidspunkt = Tidspunkt(opprettet = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)),
    @Embedded.Nullable
    val begrunnelse: Begrunnelse? = null
) : AggregateRoot { override val soknadId: UUID get() = id }

data class Tidspunkt(
    val opprettet: LocalDateTime,
    // TODO Hvordan skal diverse PUT / POSTS / REGISTER-OPPDATERINGER oppdatere denne?
    var sistEndret: LocalDateTime? = null,
    var sendtInn: LocalDateTime? = null
)

data class Begrunnelse(
    val hvorforSoke: String = "",
    val hvaSokesOm: String = ""
)
