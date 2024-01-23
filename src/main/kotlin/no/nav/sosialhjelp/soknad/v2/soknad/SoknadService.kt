package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class SoknadService(
    val soknadRepository: SoknadRepository
) {
    fun findSoknad(soknadId: UUID): Soknad {
        return soknadRepository.findById(soknadId).getOrNull()
            ?: throw IkkeFunnetException("Soknad ikke funnet")
    }
}