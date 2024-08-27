package no.nav.sosialhjelp.soknad.v2.innsendtsoknadmetadata

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.hibernate.validator.constraints.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID


@Repository
interface InnsendtSoknadMetadataRepository : UpsertRepository<InnsendtSoknadMetadata>, ListCrudRepository<InnsendtSoknadMetadata, UUID>

@Table
data class InnsendtSoknadMetadata(
    @Id override val soknadId: UUID,
    val personID: String,
    val sendt_inn_dato: LocalDateTime,
    val opprettet_dato: LocalDateTime,
//    val soknadType: SoknadType,
    // TODO Legg til søknadstype i ny modell
): DomainRoot

