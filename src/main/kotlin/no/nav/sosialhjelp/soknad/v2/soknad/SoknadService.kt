package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.INNSENDING_FEILET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SlettGamleSoknaderJob
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

interface PersonIdService {
    fun findPersonId(soknadId: UUID): String
}

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
    fun findAllSoknadIds(): List<UUID>

    fun findSoknadIdsOlderThanWithStatus(
        timestamp: LocalDateTime,
        status: SoknadStatus,
    ): List<UUID>

    fun findSoknadIdsWithStatus(status: SoknadStatus): List<UUID>

    fun deleteSoknadById(id: UUID)

    fun deleteSoknaderByIds(ids: List<UUID>)
}

interface BegrunnelseService {
    fun findBegrunnelse(soknadId: UUID): Begrunnelse

    fun updateHvaSokesOm(
        soknadId: UUID,
        hvorforSoke: String?,
        hvaSokesOm: String,
    ): Begrunnelse

    fun updateKategorier(
        soknadId: UUID,
        kategorier: Set<Kategori>,
        annet: String,
    ): Begrunnelse
}

@Transactional
@Service
class SoknadServiceImpl(
    private val soknadRepository: SoknadRepository,
    private val metadataService: SoknadMetadataService,
) : SoknadService, BegrunnelseService, SoknadJobService, PersonIdService {
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

    @Transactional
    override fun findOpenSoknadIds(personId: String): List<UUID> {
        val openSoknadIds = soknadRepository.findOpenSoknadIds(personId)

        return openSoknadIds
            .let { openIds -> metadataService.getMetadatasForIds(openIds) }
            .let { metadatas -> filterTooOld(metadatas) }
            .map { validMetadata -> validMetadata.soknadId }
            .also { validIds -> deleteTooOldOpenSoknadIds(openSoknadIds, validIds) }
    }

    private fun filterTooOld(metadatas: List<SoknadMetadata>): List<SoknadMetadata> =
        metadatas.filter { metadata ->
            when (metadata.status) {
                OPPRETTET -> metadata.tidspunkt.opprettet.isNotOlderThan(OPPRETTET_LIFESPAN)
                INNSENDING_FEILET -> metadata.tidspunkt.opprettet.isNotOlderThan(INNSENDING_FEILET_LIFESPAN)
                else -> error("Uventet status ${metadata.status} for soknad ${metadata.soknadId}")
            }
        }

    private fun LocalDateTime.isNotOlderThan(days: Long): Boolean = isAfter(LocalDateTime.now().minusDays(days))

    private fun deleteTooOldOpenSoknadIds(
        openIds: List<UUID>,
        validIds: List<UUID>,
    ) {
        openIds
            .let { it.filterNot { id -> validIds.contains(id) } }
            .also { tooOldIds ->
                if (tooOldIds.isNotEmpty()) {
                    logger.warn("Det fantes ${tooOldIds.size} åpne søknader som var for gamle. Slettes.")
                    metadataService.deleteAll(tooOldIds)
                }
            }
    }

    @Transactional(readOnly = true)
    override fun findAllSoknadIds(): List<UUID> = soknadRepository.findAll().map { it.id }

    @Transactional(readOnly = true)
    override fun findSoknadIdsOlderThanWithStatus(
        timestamp: LocalDateTime,
        status: SoknadStatus,
    ): List<UUID> {
        return soknadRepository.findSoknadIdsOlderThanWithStatus(timestamp, status)
    }

    @Transactional(readOnly = true)
    override fun findSoknadIdsWithStatus(status: SoknadStatus): List<UUID> {
        return soknadRepository.findIdsWithStatus(status)
    }

    override fun deleteSoknadById(id: UUID) {
        soknadRepository.deleteById(id)
    }

    override fun deleteSoknaderByIds(ids: List<UUID>) {
        soknadRepository.deleteAllById(ids)
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

    @Transactional
    override fun updateHvaSokesOm(
        soknadId: UUID,
        hvorforSoke: String?,
        hvaSokesOm: String,
    ): Begrunnelse =
        findOrError(soknadId)
            .copy(begrunnelse = Begrunnelse(hvorforSoke = hvorforSoke, hvaSokesOm = hvaSokesOm))
            .let { soknadRepository.save(it) }
            .begrunnelse

    @Transactional
    override fun updateKategorier(
        soknadId: UUID,
        kategorier: Set<Kategori>,
        annet: String,
    ): Begrunnelse =
        findOrError(soknadId)
            .copy(
                begrunnelse =
                    Begrunnelse(
                        kategorier =
                            Kategorier(
                                definerte = kategorier,
                                annet = annet,
                            ),
                    ),
            )
            .let { soknadRepository.save(it) }
            .begrunnelse

    override fun findPersonId(soknadId: UUID): String = findOrError(soknadId).eierPersonId

    companion object {
        private val logger by logger()

        private const val OPPRETTET_LIFESPAN = SlettGamleSoknaderJob.NUMBER_OF_DAYS
        private const val INNSENDING_FEILET_LIFESPAN = SlettGamleSoknaderJob.NUMBER_OF_DAYS
    }
}
