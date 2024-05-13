package no.nav.sosialhjelp.soknad.v2.livssituasjon.service

import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

// TODO Denne kjører med Prop.NESTED fordi den ikke må ødelegge for annen skriving
@Transactional(propagation = Propagation.NESTED)
@Service
class LivssituasjonRegisterService(private val repository: LivssituasjonRepository) {
    fun updateArbeidsforhold(
        soknadId: UUID,
        arbeidsforhold: List<Arbeidsforhold>,
    ) {
        findOrCreate(soknadId)
            .run { copy(arbeid = arbeid.copy(arbeidsforhold = arbeidsforhold)) }
            .let { repository.save(it) }
            .arbeid
    }

    private fun findOrCreate(soknadId: UUID) =
        repository.findByIdOrNull(soknadId)
            ?: repository.save(Livssituasjon(soknadId))
}
