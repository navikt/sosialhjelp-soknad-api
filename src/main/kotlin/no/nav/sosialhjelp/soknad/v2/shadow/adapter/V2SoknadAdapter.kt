package no.nav.sosialhjelp.soknad.v2.shadow.adapter

import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.soknad.findOrError
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Component
@Transactional(propagation = Propagation.NESTED)
class V2SoknadAdapter(
    private val soknadRepository: SoknadRepository,
    private val livssituasjonRepository: LivssituasjonRepository,
    private val eierRepository: EierRepository,
) {
    fun createNewSoknad(soknadId: UUID, opprettetDato: LocalDateTime, eierPersonId: String) {
        soknadRepository.save(
            Soknad(
                id = soknadId,
                tidspunkt = Tidspunkt(opprettet = opprettetDato),
                eierPersonId = eierPersonId,
            )
        )
    }

    fun saveArbeidsforhold(soknadId: UUID, arbeidsforhold: List<Arbeidsforhold>) {
        getLivssituasjon(soknadId).run {
            copy(
                arbeid = Arbeid(arbeidsforhold = arbeidsforhold),
            )
        }.also { livssituasjonRepository.save(it) }
    }

    private fun getLivssituasjon(soknadId: UUID) = livssituasjonRepository.findByIdOrNull(soknadId)
        ?: livssituasjonRepository.save(Livssituasjon(soknadId))

    fun createEier(soknadId: UUID, personalia: JsonPersonalia) {
        personalia
            .let {
                Eier(
                    soknadId = soknadId,
                    statsborgerskap = it.statsborgerskap.verdi,
                    nordiskBorger = it.nordiskBorger.verdi,
                    navn = Navn(
                        fornavn = it.navn.fornavn,
                        mellomnavn = it.navn.mellomnavn,
                        etternavn = it.navn.etternavn
                    )
                )
            }
            .also { eier -> eierRepository.save(eier) }
    }

    fun setInnsendingstidspunkt(soknadId: UUID, innsendingsTidspunkt: LocalDateTime) {
        soknadRepository.findOrError(soknadId)
            .run { copy(tidspunkt = tidspunkt.copy(sendtInn = innsendingsTidspunkt)) }
            .also { soknadRepository.save(it) }
    }
}
