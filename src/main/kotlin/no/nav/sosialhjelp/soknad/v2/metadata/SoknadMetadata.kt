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
    fun hentEldreEnn(timestamp: LocalDateTime): List<UUID>
}

@Table
data class SoknadMetadata(
    @Id val soknadId: UUID,
    val personId: String,
    val status: SoknadStatus = SoknadStatus.OPPRETTET,
    val opprettet: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    val innsendt: LocalDateTime? = null,
    @Embedded.Nullable
    val mottaker: NavMottaker? = null,
    val digisosId: UUID? = null,
) : DomainRoot {
    override fun getDbId() = soknadId

    init {
        status.validate(this)
    }
}

data class NavMottaker(
    val kommunenummer: String,
    val bydelsnummer: String? = null,
) {
    init {
        this.validate()
    }
}

enum class SoknadStatus {
    OPPRETTET,

    // TODO Skal vi ta vare på metadata for avbrutte soknader? Til hva ?
    AVBRUTT,
    SENDT,
    MOTTATT_FSL,
}

private fun NavMottaker.validate() {
    if (kommunenummer.length != 4) error("Kommunenummer ikke 4 siffer")
}

private fun SoknadStatus.validate(metadata: SoknadMetadata) {
    if (this == SoknadStatus.SENDT || this == SoknadStatus.MOTTATT_FSL) {
        if (metadata.innsendt == null) error("Mangler innsendt dato for ferdig søknad.")
        if (metadata.mottaker == null) error("Mangler mottaker for ferdig søknad.")
        if (metadata.digisosId == null) error("Mangler digisosId for ferdig søknad.")
    }
}
