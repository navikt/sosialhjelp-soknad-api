package no.nav.sosialhjelp.soknad.service

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sosialhjelp.soknad.model.Arbeid
import no.nav.sosialhjelp.soknad.repository.ArbeidRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ArbeidService(
    private val arbeidRepository: ArbeidRepository
) {

    @Transactional(readOnly = true)
    fun hentArbeid(soknadId: UUID): Arbeid? {
        return arbeidRepository.findByIdOrNull(soknadId)
    }

    @Transactional
    fun updateArbeid(soknadId: UUID, kommentarArbeid: String?): Arbeid {

        val arbeid = arbeidRepository.findByIdOrNull(soknadId)?.apply {
            this.kommentarArbeid = kommentarArbeid
        } ?: Arbeid(soknadId = soknadId, kommentarArbeid = kommentarArbeid)

        return arbeidRepository.save(arbeid)
    }
}
