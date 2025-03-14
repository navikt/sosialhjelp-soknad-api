package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

interface SoknadService {
    fun findOrError(soknadId: UUID): Soknad

    fun createSoknad(
        eierId: String,
        soknadId: UUID,
        // TODO Dokumentasjonen på filformatet sier at dette skal være UTC
        opprettetDato: LocalDateTime,
        kortSoknad: Boolean,
    ): UUID

    fun deleteSoknad(soknadId: UUID)

    fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime?,
    )

    fun hasSoknadNewerThan(
        eierId: String,
        tidspunkt: LocalDateTime,
    ): Boolean

    fun erKortSoknad(soknadId: UUID): Boolean

    fun updateKortSoknad(
        soknadId: UUID,
        kortSoknad: Boolean,
    )

    fun getSoknadOrNull(soknadId: UUID): Soknad?
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
) : SoknadService, BegrunnelseService {
    @Transactional(readOnly = true)
    override fun findOrError(soknadId: UUID): Soknad =
        soknadRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Soknad finnes ikke")

    override fun createSoknad(
        eierId: String,
        soknadId: UUID,
        opprettetDato: LocalDateTime,
        kortSoknad: Boolean,
    ): UUID =
        Soknad(
            id = soknadId,
            tidspunkt = Tidspunkt(opprettet = opprettetDato),
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

    override fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime?,
    ) {
        findOrError(soknadId)
            .run { copy(tidspunkt = tidspunkt.copy(sendtInn = innsendingsTidspunkt)) }
            .also { soknadRepository.save(it) }
    }

    @Transactional(readOnly = true)
    override fun getSoknadOrNull(soknadId: UUID) = soknadRepository.findByIdOrNull(soknadId)

    @Transactional(readOnly = true)
    override fun hasSoknadNewerThan(
        eierId: String,
        tidspunkt: LocalDateTime,
    ): Boolean = soknadRepository.findNewerThan(eierId, tidspunkt).any()

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
