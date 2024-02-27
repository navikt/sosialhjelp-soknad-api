package no.nav.sosialhjelp.soknad.v2.shadow.adapter

import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Tidspunkt
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
class SoknadAdapter(
    private val soknadRepository: SoknadRepository,
) {
    fun createNewSoknad(soknadId: UUID, opprettetDato: LocalDateTime, eierPersonId: String) {
        soknadRepository.save(
            Soknad(
                id = soknadId,
                tidspunkt = Tidspunkt(opprettet = opprettetDato),
                eierPersonId = eierPersonId
            )
        )
    }
}
