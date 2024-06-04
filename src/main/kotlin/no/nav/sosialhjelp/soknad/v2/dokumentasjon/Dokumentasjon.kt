package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DokumentasjonRepository : UpsertRepository<Dokumentasjon>, ListCrudRepository<Dokumentasjon, UUID> {
    fun findAllBySoknadId(soknadId: UUID): List<Dokumentasjon>

    fun findBySoknadIdAndType(
        soknadId: UUID,
        type: OkonomiType,
    ): Dokumentasjon?
}

@Table
data class Dokumentasjon(
    @Id val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OkonomiType,
    val status: DokumentasjonStatus = DokumentasjonStatus.FORVENTET,
    val dokumenter: Set<Dokument> = emptySet(),
) : DomainRoot {
    override fun getDbId() = id
}

data class Dokument(
    val filnavn: String,
    val sha512: String,
)

enum class DokumentasjonStatus {
    LASTET_OPP,
    FORVENTET,
    LEVERT_TIDLIGERE,
}
