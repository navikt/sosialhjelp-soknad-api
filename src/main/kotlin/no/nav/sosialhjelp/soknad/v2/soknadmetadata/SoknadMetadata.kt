package no.nav.sosialhjelp.soknad.v2.soknadmetadata

import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID


@Repository
interface SoknadMetadataRepository : UpsertRepository<SoknadMetadata>, ListCrudRepository<SoknadMetadata, UUID>

@Table
data class SoknadMetadata(
    @Id
    override val soknadID: UUID,
    val personID: String,
    val sendt_inn_dato: LocalDateTime,
    val opprettet_dato: LocalDateTime,
//    val soknadType: SoknadType,
    // TODO Legg til søknadstype i ny modell
)

