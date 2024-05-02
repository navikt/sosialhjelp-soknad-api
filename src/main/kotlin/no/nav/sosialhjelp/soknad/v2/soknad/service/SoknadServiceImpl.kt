package no.nav.sosialhjelp.soknad.v2.soknad.service

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.SendSoknadHandler
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Tidspunkt
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.soknad.findOrError

@Service
@Transactional
class SoknadServiceImpl(
    private val soknadRepository: SoknadRepository,
    private val mellomlagringService: MellomlagringService,
    private val sendSoknadHandler: SendSoknadHandler,
) : SoknadService, BegrunnelseService {
    @Transactional(readOnly = true)
    override fun findOrError(soknadId: UUID): Soknad = soknadRepository.findByIdOrNull(soknadId)
        ?: throw IkkeFunnetException("Soknad finnes ikke")

    override fun createSoknad(
        eierId: String,
        soknadId: UUID?,
        opprettetDato: LocalDateTime?,
    ): UUID {
        return Soknad(
            id = soknadId ?: UUID.randomUUID(),
            tidspunkt = Tidspunkt(opprettet = opprettetDato ?: LocalDateTime.now()),
            eierPersonId = eierId,
        )
            .let { soknadRepository.save(it) }
            .id
    }

    override fun deleteSoknad(soknadId: UUID) {
        findOrError(soknadId).also {
            soknadRepository.delete(it)
        }
        mellomlagringService.deleteAll(soknadId)
    }

    override fun sendSoknad(soknadId: UUID): UUID {
        val digisosId: UUID =
            findOrError(soknadId).run {
                tidspunkt.sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                soknadRepository.save(this)

                sendSoknadHandler.doSendAndReturnDigisosId(this)
            }
        log.info("Sletter innsendt Soknad $soknadId")
        soknadRepository.deleteById(soknadId)

        return digisosId
    }

    override fun slettSoknad(soknadId: UUID) {
        soknadRepository.findByIdOrNull(soknadId)?.let { soknadRepository.delete(it) }
            ?: log.warn("Soknad V2 finnes ikke.")
    }

    override fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime,
    ) {
        soknadRepository.findOrError(soknadId)
            .run {
                this.tidspunkt
                    .copy(sendtInn = innsendingsTidspunkt)
                    .let { tidCopy -> this.copy(tidspunkt = tidCopy) }
                    .let { sokCopy -> soknadRepository.save(sokCopy) }
            }
    }

    override fun findBegrunnelse(soknadId: UUID) = findOrError(soknadId).begrunnelse

    override fun updateBegrunnelse(
        soknadId: UUID,
        begrunnelse: Begrunnelse,
    ): Begrunnelse {
        return findOrError(soknadId)
            .copy(begrunnelse = begrunnelse)
            .let { soknadRepository.save(it) }
            .begrunnelse
    }

    companion object {
        private val log by logger()
    }
}
