package no.nav.sosialhjelp.soknad.v2.innsendtsoknadmetadata

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID


@Repository
interface InnsendtSoknadMetadataRepository : UpsertRepository<InnsendtSoknadmetadata>, ListCrudRepository<InnsendtSoknadmetadata, UUID>

@Table
data class InnsendtSoknadmetadata(
    @Id override val soknadId: UUID,
    val personId: String,
    val sendt_inn_dato: LocalDateTime,
    val opprettet_dato: LocalDateTime,
//    val soknadType: SoknadType,
    // TODO Legg til søknadstype i ny modell
): DomainRoot

