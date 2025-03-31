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

    @Query("SELECT * FROM soknad_metadata WHERE person_id = :personId AND status IN ('SENDT', 'MOTTATT_FSL')")
    fun findSendteSoknaderForPerson(personId: String): List<SoknadMetadata>

    @Query("SELECT * FROM soknad_metadata WHERE soknad_id IN (:soknadIds) AND opprettet < :timestamp")
    fun findOlderThan(
        soknadIds: List<UUID>,
        timestamp: LocalDateTime,
    ): List<SoknadMetadata>

    @Query("SELECT soknad_id FROM soknad_metadata WHERE opprettet < :timestamp AND status = :status")
    fun findOlderThanWithStatus(
        timestamp: LocalDateTime,
        status: SoknadStatus,
    ): List<UUID>
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
    var sendtInn: LocalDateTime? = null,
)

enum class SoknadStatus {
    OPPRETTET,

    // TODO Skal vi ta vare på metadata for avbrutte soknader? Til hva ?
    AVBRUTT,

    // TODO Skal vi ta vare på metadata for ikke innsendte soknader over 14 dager? Isåfall må status endres
    UTGATT,
    INNSENDING_FEILET,
    SENDT,
    MOTTATT_FSL,
}

private fun SoknadStatus.validate(metadata: SoknadMetadata) {
    if (this == SoknadStatus.SENDT || this == SoknadStatus.MOTTATT_FSL) {
        if (metadata.tidspunkt.sendtInn == null) error("Mangler innsendt dato for ferdig søknad.")
        // Må disable disse fordi etter migrering til ny datamodell, så har vi ikke disse verdiene.
        // TODO: Fiks når de har blitt sletta etter 200 dager: 23. august 2025
        // if (metadata.mottakerKommunenummer == null) error("Mangler mottaker for ferdig søknad.")
        // if (metadata.digisosId == null) error("Mangler digisosId for ferdig søknad.")
        // if (metadata.mottakerKommunenummer.length != 4) error("Kommunenummer ikke 4 siffer")
    }
}
