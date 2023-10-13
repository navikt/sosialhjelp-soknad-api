package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.model.Soknad
import no.nav.sosialhjelp.soknad.repository.SoknadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SoknadService(
    private val soknadRepository: SoknadRepository
) {
    @Transactional
    fun opprettNySoknad(soknadId: UUID) = soknadRepository.save(Soknad(soknadId))

    @Transactional(readOnly = true)
    fun hentSoknad(soknadId: UUID) = soknadRepository.findSoknad(soknadId)
}