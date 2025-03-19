package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface SoknadService {
    fun findOrError(soknadId: UUID): Soknad

    fun createSoknad(
        eierId: String,
        soknadId: UUID,
        kortSoknad: Boolean,
    ): UUID

    fun deleteSoknad(soknadId: UUID)

    fun erKortSoknad(soknadId: UUID): Boolean

    fun updateKortSoknad(
        soknadId: UUID,
        kortSoknad: Boolean,
    )

    fun getSoknadOrNull(soknadId: UUID): Soknad?

    fun findOpenSoknadIds(fnr: String): List<UUID>
}

interface SoknadJobService {
    fun getAllSoknader(): List<Soknad>

    fun deleteAllByIdCatchError(ids: List<UUID>): Int
}

interface BegrunnelseService {
    fun findBegrunnelse(soknadId: UUID): Begrunnelse

    fun updateBegrunnelse(
        soknadId: UUID,
        begrunnelse: Begrunnelse,
    ): Begrunnelse
}

@Service
@Transactional
class SoknadServiceImpl(
    private val soknadRepository: SoknadRepository,
) : SoknadService, BegrunnelseService, SoknadJobService {
    @Transactional(readOnly = true)
    override fun findOrError(soknadId: UUID): Soknad =
        soknadRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Soknad finnes ikke")

    override fun createSoknad(
        eierId: String,
        soknadId: UUID,
        kortSoknad: Boolean,
    ): UUID =
        Soknad(
            id = soknadId,
            eierPersonId = eierId,
            kortSoknad = kortSoknad,
        ).let { soknadRepository.save(it) }
            .id

    override fun deleteSoknad(soknadId: UUID) {
        soknadRepository
            .findByIdOrNull(soknadId)
            ?.let { soknadRepository.delete(it) }
            ?: logger.warn("Kan ikke slette soknad. Finnes ikke.")
    }

    @Transactional(readOnly = true)
    override fun getSoknadOrNull(soknadId: UUID) = soknadRepository.findByIdOrNull(soknadId)

    @Transactional(readOnly = true)
    override fun findOpenSoknadIds(fnr: String): List<UUID> = soknadRepository.findOpenSoknadIds(fnr)

    @Transactional(readOnly = true)
    override fun getAllSoknader(): List<Soknad> = soknadRepository.findAll()

    /**
     * Fail-safe sletting så alt som kan slettes slettes
     */
    @Transactional
    override fun deleteAllByIdCatchError(ids: List<UUID>): Int {
        var antallSlettet = 0

        ids.forEach { id ->
            runCatching { soknadRepository.deleteById(id) }
                .onSuccess { antallSlettet++ }
                .onFailure { logger.error("Kunne ikke slette Soknad (id=$id)", it) }
        }

        return antallSlettet
    }

    @Transactional(readOnly = true)
    override fun erKortSoknad(soknadId: UUID): Boolean = findOrError(soknadId).kortSoknad

    override fun updateKortSoknad(
        soknadId: UUID,
        kortSoknad: Boolean,
    ) {
        val soknad = findOrError(soknadId)
        val updatedSoknad = soknad.copy(kortSoknad = kortSoknad)
        soknadRepository.save(updatedSoknad)
    }

    @Transactional(readOnly = true)
    override fun findBegrunnelse(soknadId: UUID) = findOrError(soknadId).begrunnelse

    override fun updateBegrunnelse(
        soknadId: UUID,
        begrunnelse: Begrunnelse,
    ): Begrunnelse =
        findOrError(soknadId)
            .copy(begrunnelse = begrunnelse)
            .let { soknadRepository.save(it) }
            .begrunnelse

    companion object {
        private val logger by logger()
    }
}
