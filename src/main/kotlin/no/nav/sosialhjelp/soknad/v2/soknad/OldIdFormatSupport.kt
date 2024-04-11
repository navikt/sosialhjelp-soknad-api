package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.innsending.SenderUtils
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Table
data class IdFormatMap(
    @Id val soknadId: UUID,
    val idOldFormat: String
)

@Component
class OldIdFormatSupportHandler(
    private val aggregateTemplate: JdbcAggregateTemplate,
    private val jdbcTemplate: JdbcTemplate,
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    fun findByUUID(id: String): IdFormatMap? = aggregateTemplate.findById(UUID.fromString(id), IdFormatMap::class.java)

    fun createAndMap(behandlingsId: String): IdFormatMap {
        return soknadMetadataRepository.hent(behandlingsId)
            ?.let {
                aggregateTemplate.insert(
                    IdFormatMap(soknadId = UUID.fromString(behandlingsId), idOldFormat = createOldFormatId())
                )
            } ?: error("SoknadMetadata finnes ikke ved opprettelse av IdFormatMap")
    }

    fun deleteAllByIds(ids: List<String>) {
        aggregateTemplate.deleteAllById(
            ids.map { UUID.fromString(it) },
            IdFormatMap::class.java
        )
    }

    private fun createOldFormatId(): String {
        return getNextId()?.let { SenderUtils.lagBehandlingsId(it) }
            ?: error("Feil ved henting av neste verdi fra sequence")
    }

    fun getNextId(): Long? = jdbcTemplate.queryForObject("SELECT nextval('id_sequence')", Long::class.java)
}
