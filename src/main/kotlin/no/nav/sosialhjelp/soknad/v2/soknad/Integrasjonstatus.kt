package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.UUID

@Repository
interface IntegrasjonstatusRepository : UpsertRepository<Integrasjonstatus>, ListCrudRepository<Integrasjonstatus, UUID>

@Service
class IntegrasjonStatusService(private val repository: IntegrasjonstatusRepository) {
    fun hasHusbankenFailed(soknadId: UUID): Boolean? = repository.findByIdOrNull(soknadId)?.feilStotteHusbanken

    fun setUtbetalingerFraNavStatus(
        soknadId: UUID,
        feilet: Boolean,
    ): Integrasjonstatus {
        return findOrCreate(soknadId)
            .copy(feilUtbetalingerNav = feilet)
            .let { repository.save(it) }
    }

    fun setStotteHusbankenStatus(
        soknadId: UUID,
        feilet: Boolean,
    ): Integrasjonstatus {
        return findOrCreate(soknadId)
            .copy(feilStotteHusbanken = feilet)
            .let { repository.save(it) }
    }

    fun setInntektSkatteetatenStatus(
        soknadId: UUID,
        feilet: Boolean,
    ): Integrasjonstatus {
        return findOrCreate(soknadId)
            .copy(feilInntektSkatteetaten = feilet)
            .let { repository.save(it) }
    }

    private fun findOrCreate(soknadId: UUID): Integrasjonstatus {
        return repository.findByIdOrNull(soknadId) ?: repository.save(Integrasjonstatus(soknadId))
    }
}

@Table
data class Integrasjonstatus(
    @Id
    val soknadId: UUID,
    val feilUtbetalingerNav: Boolean = false,
    val feilInntektSkatteetaten: Boolean = false,
    val feilStotteHusbanken: Boolean = false,
) : DomainRoot {
    override fun getDbId() = soknadId
}
