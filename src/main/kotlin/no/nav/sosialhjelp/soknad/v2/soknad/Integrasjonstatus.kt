package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.v2.config.repository.AggregateRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IntegrasjonstatusRepository : UpsertRepository<Integrasjonstatus>, ListCrudRepository<Integrasjonstatus, UUID>

@Table
data class Integrasjonstatus(
    @Id
    override val soknadId: UUID,
    val feilUtbetalingerNav: Boolean = false,
    val feilInntektSkatteetaten: Boolean = false,
    val feilStotteHusbanken: Boolean = false
) : AggregateRoot
