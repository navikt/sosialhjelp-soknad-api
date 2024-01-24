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
            ?: throw IkkeFunnetException("Soknad finnes ikke")
    }

    fun deleteSoknad(soknadId: UUID) {
        val soknad = soknadRepository.findById(soknadId).getOrNull()
            ?: throw IkkeFunnetException("Soknad finnes ikke")

        soknadRepository.delete(soknad)
        if (soknadRepository.existsById(soknadId)) {
            throw IkkeFunnetException("Kunne ikke slette soknad")
        }
    }
}
