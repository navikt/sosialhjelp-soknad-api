package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.SendSoknadHandler
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
@Transactional
class SoknadServiceImpl(
    private val soknadRepository: SoknadRepository,
    private val mellomlagringService: MellomlagringService,
    private val sendSoknadHandler: SendSoknadHandler,
): ServiceSoknad, BegrunnelseService, SoknadShadowAdapterService {
    @Transactional(readOnly = true)
    override fun findSoknad(soknadId: UUID): Soknad = getSoknadOrThrowException(soknadId)

    override fun createSoknad(
        eierId: String,
        soknadId: UUID?,
        opprettetDato: LocalDateTime?,
    ): UUID {
        return Soknad(
            id = soknadId ?: UUID.randomUUID(),
            tidspunkt = Tidspunkt(opprettet = opprettetDato ?: LocalDateTime.now() ),
            eierPersonId = eierId,
        )
            .let { soknadRepository.save(it) }
            .id
    }

    override fun deleteSoknad(soknadId: UUID) {
        getSoknadOrThrowException(soknadId).also {
            soknadRepository.delete(it)
        }
        mellomlagringService.deleteAll(soknadId)
    }

    override fun sendSoknad(id: UUID): UUID {
        val digisosId: UUID =
            getSoknadOrThrowException(id).run {
                tidspunkt.sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                soknadRepository.save(this)

                sendSoknadHandler.doSendAndReturnDigisosId(this)
            }
        log.info("Sletter innsendt Soknad $id")
        soknadRepository.deleteById(id)

        return digisosId
    }

    override fun slettSoknad(soknadId: UUID) {
        soknadRepository.findByIdOrNull(soknadId)?.let { soknadRepository.delete(it) }
            ?: log.warn("Soknad V2 finnes ikke: $soknadId")
    }

    override fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime,
    ) {
        soknadRepository.findByIdOrNull(soknadId)
            ?.run {
                this.tidspunkt
                    .copy(sendtInn = innsendingsTidspunkt)
                    .let { tidCopy -> this.copy(tidspunkt = tidCopy) }
                    .let { sokCopy -> soknadRepository.save(sokCopy) }
            }
            ?: log.error("Fant ikke Soknad V2")
    }

    private fun getSoknadOrThrowException(soknadId: UUID): Soknad {
        return soknadRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Soknad finnes ikke")
    }

    override fun findBegrunnelse(soknadId: UUID): Begrunnelse {
        return getSoknadOrThrowException(soknadId).begrunnelse
    }

    override fun updateBegrunnelse(
        soknadId: UUID,
        begrunnelse: Begrunnelse,
    ): Begrunnelse {
        return getSoknadOrThrowException(soknadId)
            .copy(begrunnelse = begrunnelse)
            .let { soknadRepository.save(it) }
            .begrunnelse
    }

    companion object {
        private val log by logger()
    }
}

interface ServiceSoknad {
    fun findSoknad(soknadId: UUID): Soknad
    fun createSoknad(
        eierId: String,
        soknadId: UUID? = null,
        // TODO Dokumentasjonen på filformatet sier at dette skal være UTC
        opprettetDato: LocalDateTime? = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    ): UUID

    fun sendSoknad(id: UUID): UUID
    fun deleteSoknad(soknadId: UUID)
    fun slettSoknad(soknadId: UUID)

}

interface BegrunnelseService {
    fun findBegrunnelse(soknadId: UUID): Begrunnelse
    fun updateBegrunnelse(soknadId: UUID, begrunnelse: Begrunnelse): Begrunnelse
}

interface SoknadShadowAdapterService {
    fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime,
    )
}
