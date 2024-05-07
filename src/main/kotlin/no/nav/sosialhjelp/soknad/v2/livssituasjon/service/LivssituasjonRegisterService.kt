package no.nav.sosialhjelp.soknad.v2.livssituasjon.service

import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service


@Service
class LivssituasjonRegisterService(private val repository: LivssituasjonRepository) {
    fun updateArbeidsforhold(
        soknadId: UUID,
        arbeidsforhold: List<Arbeidsforhold>,
    ) {
        findOrCreate(soknadId)
            .run {
                (this.arbeid ?: Arbeid())
                    .let { arb -> this.copy(arbeid = arb.copy(arbeidsforhold = arbeidsforhold)) }
                    .let { livs -> repository.save(livs) }
            }
            .arbeid ?: error("Arbeid kunne ikke lagres")
    }

    private fun findOrCreate(soknadId: UUID) =
        repository.findByIdOrNull(soknadId)
            ?: repository.save(Livssituasjon(soknadId))
}
