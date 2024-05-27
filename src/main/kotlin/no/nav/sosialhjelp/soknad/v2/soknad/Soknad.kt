package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Repository
interface SoknadRepository : UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID> {
    @Query("SELECT * FROM soknad WHERE opprettet < :timestamp")
    fun findOlderThan(timestamp: LocalDateTime): List<Soknad>
}

@Table
data class Soknad(
    @Id
    val id: UUID = UUID.randomUUID(),
    val eierPersonId: String,
    @Embedded.Empty
    val tidspunkt: Tidspunkt = Tidspunkt(opprettet = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)),
    @Embedded.Empty
    val begrunnelse: Begrunnelse = Begrunnelse(),
) : DomainRoot {
    override val soknadId: UUID get() = id
}

data class Tidspunkt(
    val opprettet: LocalDateTime,
    // TODO Hvordan skal diverse PUT / POSTS / REGISTER-OPPDATERINGER oppdatere denne?
    var sistEndret: LocalDateTime? = null,
    var sendtInn: LocalDateTime? = null,
)

data class Begrunnelse(
    val hvorforSoke: String = "",
    val hvaSokesOm: String = "",
)
