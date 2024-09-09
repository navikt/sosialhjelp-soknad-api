package no.nav.sosialhjelp.soknad.v2.situasjonsendring

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SituasjonsendringRepository :
    UpsertRepository<Situasjonsendring>,
    ListCrudRepository<Situasjonsendring, UUID>

@Table
data class Situasjonsendring(
    @Id
    val soknadId: UUID,
    val hvaErEndret: String?,
    val endring: Boolean?,
) : DomainRoot {
    override fun getDbId() = soknadId
}
