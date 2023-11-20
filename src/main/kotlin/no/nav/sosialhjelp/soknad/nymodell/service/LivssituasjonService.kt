package no.nav.sosialhjelp.soknad.nymodell.service

import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.ArbeidsforholdRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.BosituasjonRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Utdanning
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.UtdanningRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class LivssituasjonService(
    private val arbeidsforholdRepository: ArbeidsforholdRepository,
    private val bosituasjonRepository: BosituasjonRepository,
    private val utdanningRepository: UtdanningRepository
) {

    fun hentArbeidsforhold(soknadId: UUID) = arbeidsforholdRepository.findAllBySoknadId(soknadId)

    fun hentBosituasjon(soknadId: UUID): Bosituasjon? {
        return bosituasjonRepository.findByIdOrNull(soknadId)
    }

    fun oppdaterBosituasjon(bosituasjon: Bosituasjon): Bosituasjon {
        return bosituasjonRepository.save(bosituasjon)
    }

    fun hentUtdanning(soknadId: UUID): Utdanning? {
        return utdanningRepository.findByIdOrNull(soknadId)
    }

    fun updateUtdanning(utdanning: Utdanning): Utdanning {
        return utdanningRepository.save(utdanning)
    }
}
