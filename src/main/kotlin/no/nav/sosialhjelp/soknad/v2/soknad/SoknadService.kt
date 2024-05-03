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
class SoknadService(
    private val soknadRepository: SoknadRepository,
    private val mellomlagringService: MellomlagringService,
    private val sendSoknadHandler: SendSoknadHandler,
) {
    @Transactional(readOnly = true)
    fun getSoknad(soknadId: UUID): Soknad = getSoknadOrThrowException(soknadId)

    fun createSoknad(
        eierId: String,
        soknadId: UUID? = null,
        opprettetDato: LocalDateTime? = null,
    ): UUID {
        return Soknad(
            id = soknadId ?: UUID.randomUUID(),
            tidspunkt = Tidspunkt(opprettet = opprettetDato ?: LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
            eierPersonId = eierId,
        )
            .let { soknadRepository.save(it) }
            .id
    }

    fun deleteSoknad(soknadId: UUID) {
        getSoknadOrThrowException(soknadId).also {
            soknadRepository.delete(it)
        }
        mellomlagringService.deleteAll(soknadId)
    }

    fun sendSoknad(id: UUID): UUID {
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

    fun updateBegrunnelse(
        soknadId: UUID,
        begrunnelse: Begrunnelse,
    ): Begrunnelse {
        return getSoknadOrThrowException(soknadId).run {
            copy(begrunnelse = begrunnelse)
                .also { soknadRepository.save(it) }
                .let { it.begrunnelse!! }
        }
    }

    fun slettSoknad(soknadId: UUID) {
        soknadRepository.findByIdOrNull(soknadId)?.let { soknadRepository.delete(it) }
            ?: log.warn("Soknad V2 finnes ikke.")
    }

    fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
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

    companion object {
        private val log by logger()
    }
}
