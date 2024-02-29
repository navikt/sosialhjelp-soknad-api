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
import java.util.*

@Service
@Transactional
class SoknadService(
    private val soknadRepository: SoknadRepository,
    private val mellomlagringService: MellomlagringService,
    private val sendSoknadHandler: SendSoknadHandler
) {
    @Transactional(readOnly = true)
    fun getSoknad(soknadId: UUID): Soknad = getSoknadOrThrowException(soknadId)

    fun createSoknad(eierId: String): UUID {
        return soknadRepository.save(Soknad(eierPersonId = eierId)).id
    }

    fun deleteSoknad(soknadId: UUID) {
        getSoknadOrThrowException(soknadId).also {
            soknadRepository.delete(it)
        }
        mellomlagringService.deleteAll(soknadId)
    }

    fun sendSoknad(id: UUID): UUID {
        val digisosId: UUID = getSoknadOrThrowException(id).run {
            tidspunkt.sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
            soknadRepository.save(this)

            sendSoknadHandler.doSendAndReturnDigisosId(this)
        }
        log.info("Sletter innsendt Soknad $id")
        soknadRepository.deleteById(id)

        return digisosId
    }

    fun updateBegrunnelse(soknadId: UUID, begrunnelse: Begrunnelse): Begrunnelse {
        return getSoknadOrThrowException(soknadId)
            .copy(begrunnelse = begrunnelse)
            .also { soknadRepository.save(it) }
            .begrunnelse
    }

    private fun getSoknadOrThrowException(soknadId: UUID): Soknad {
        return soknadRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Soknad finnes ikke")
    }

    companion object {
        private val log by logger()
    }
}
