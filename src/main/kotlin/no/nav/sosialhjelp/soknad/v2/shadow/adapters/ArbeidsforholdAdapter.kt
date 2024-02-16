package no.nav.sosialhjelp.soknad.v2.shadow.adapters

import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class ArbeidsforholdAdapter(
    private val livssituasjonRepository: LivssituasjonRepository
) {
    fun saveArbeidsforhold(soknadId: UUID, arbeidsforhold: List<Arbeidsforhold>) {
        getLivssituasjon(soknadId).run {
            copy(
                arbeid = Arbeid(arbeidsforhold = arbeidsforhold),
            )
        }.also { livssituasjonRepository.save(it) }
    }

    private fun getLivssituasjon(soknadId: UUID) = livssituasjonRepository.findByIdOrNull(soknadId)
        ?: livssituasjonRepository.save(Livssituasjon(soknadId))
}
