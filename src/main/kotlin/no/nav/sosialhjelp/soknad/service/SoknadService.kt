package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.model.BegrunnelseDto
import no.nav.sosialhjelp.soknad.model.NySoknadDto
import no.nav.sosialhjelp.soknad.model.Soknad
import no.nav.sosialhjelp.soknad.model.SoknadDto
import no.nav.sosialhjelp.soknad.repository.SoknadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SoknadService(
    private val soknadRepository: SoknadRepository
) {
    @Transactional
    fun opprettNySoknad(): NySoknadDto {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val nySoknad = soknadRepository.save(Soknad(eier = eier))
        return NySoknadDto(soknadId = nySoknad.id)
    }

    @Transactional
    fun opprettNySoknad(soknadId: UUID): NySoknadDto {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        soknadRepository.save(Soknad(id = soknadId, eier = eier))
        return NySoknadDto(soknadId = soknadId)
    }

    @Transactional(readOnly = true)
    fun hentSoknad(soknadId: UUID): SoknadDto =
        soknadRepository.findById(soknadId).get().let {
            SoknadDto(
                soknadId = it.id,
                innsendingsTidspunkt = it.innsendingstidspunkt
            )
        }

    @Transactional(readOnly = true)
    fun hentBegrunnelse(soknadId: UUID): BegrunnelseDto =
        soknadRepository.findById(soknadId).get().let {
            BegrunnelseDto(
                hvorforSoke = it.hvorforSoke,
                hvaSokesOm = it.hvaSokesOm
            )
        }

    @Transactional
    fun updateBegrunnelse(soknadId: UUID, begrunnelseDto: BegrunnelseDto) {
        val soknad = soknadRepository.findById(soknadId).get()
        soknad.hvorforSoke = begrunnelseDto.hvorforSoke
        soknad.hvaSokesOm = begrunnelseDto.hvaSokesOm

        soknadRepository.save(soknad)
    }
}
