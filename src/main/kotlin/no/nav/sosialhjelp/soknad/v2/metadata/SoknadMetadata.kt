package no.nav.sosialhjelp.soknad.v2.metadata

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
interface SoknadMetadataRepository : UpsertRepository<SoknadMetadata>, ListCrudRepository<SoknadMetadata, UUID> {
    @Query("select soknad_id from soknad_metadata where opprettet < :timestamp")
    fun findSoknadIdsOlderThan(timestamp: LocalDateTime): List<UUID>

    fun findByPersonId(personId: String): List<SoknadMetadata>

    @Query("select * from soknad_metadata where status in (:status)")
    fun findMetadataByStatus(status: List<SoknadStatus>): List<SoknadMetadata>
}

@Table
data class SoknadMetadata(
    @Id val soknadId: UUID,
    val personId: String,
    val status: SoknadStatus = SoknadStatus.OPPRETTET,
    @Embedded.Empty
    val tidspunkt: Tidspunkt = Tidspunkt(),
    val mottakerKommunenummer: String? = null,
    val digisosId: UUID? = null,
    val soknadType: SoknadType = SoknadType.STANDARD,
) : DomainRoot {
    override fun getDbId() = soknadId

    init {
        status.validate(this)
    }
}

enum class SoknadType {
    STANDARD,
    KORT,
}

data class Tidspunkt(
    val opprettet: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    var sistEndret: LocalDateTime = opprettet,
    val sendtInn: LocalDateTime? = null,
)

enum class SoknadStatus {
    OPPRETTET,
    INNSENDING_FEILET,
    SENDT,
    MOTTATT_FSL,
    MANUELT_KVITTERT_UT,
}

private fun SoknadStatus.validate(metadata: SoknadMetadata) {
    if (this in listOf(SoknadStatus.SENDT, SoknadStatus.MOTTATT_FSL, SoknadStatus.MANUELT_KVITTERT_UT)) {
        if (metadata.tidspunkt.sendtInn == null) error("Mangler innsendt dato for ferdig søknad.")
        if (metadata.mottakerKommunenummer == null) error("Mangler mottaker for ferdig søknad.")
        if (metadata.digisosId == null) error("Mangler digisosId for ferdig søknad.")
        if (metadata.mottakerKommunenummer.length != 4) error("Kommunenummer ikke 4 siffer")
    }
}
