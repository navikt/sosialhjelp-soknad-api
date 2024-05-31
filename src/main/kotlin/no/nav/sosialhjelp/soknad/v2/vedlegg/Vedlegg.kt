package no.nav.sosialhjelp.soknad.v2.vedlegg

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VedleggRepository : UpsertRepository<Vedlegg>, ListCrudRepository<Vedlegg, UUID> {
    fun findAllBySoknadId(soknadId: UUID): List<Vedlegg>
}

@Table
data class Vedlegg(
    @Id val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OkonomiType,
    val status: VedleggStatus = VedleggStatus.KREVES,
    val filer: Set<Fil> = emptySet(),
) : DomainRoot {
    override fun getDbId() = id
}

data class Fil(
    val filnavn: String,
    val sha512: String,
)

enum class VedleggStatus {
    LASTET_OPP,
    KREVES,
    LEVERT_TIDLIGERE,
}
