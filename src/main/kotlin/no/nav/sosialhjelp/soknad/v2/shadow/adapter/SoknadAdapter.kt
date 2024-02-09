package no.nav.sosialhjelp.soknad.v2.shadow.adapter

import no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Tidspunkt
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class SoknadAdapter(
    private val soknadRepository: SoknadRepository,
) {
    fun createNewSoknad(soknadId: UUID, opprettetDato: LocalDateTime, eier: Eier) {
        soknadRepository.save(
            Soknad(
                id = soknadId,
                tidspunkt = Tidspunkt(opprettet = opprettetDato),
                eier = eier,
            )
        )
    }

    fun handleArbeidsforholdList(soknadId: UUID, arbeidsforholdList: List<Arbeidsforhold>) {
        soknadRepository.findByIdOrNull(soknadId)
            ?.run {

                this.arbeidsForhold = arbeidsforholdList
                soknadRepository.save(this)

            }
    }

    fun addTelefonnummer(soknadId: UUID, telefonnummer: String) {
        soknadRepository.findByIdOrNull(soknadId)
            ?.run {
                eier.telefonnummer = telefonnummer
                soknadRepository.save(this)
            }
            ?: throw IllegalArgumentException("Skyggeproduksjon: Lagring av telefonnummer fra register feilet")
    }
}


