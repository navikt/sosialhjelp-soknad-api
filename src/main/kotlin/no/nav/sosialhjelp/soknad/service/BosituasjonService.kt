package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.model.BosituasjonDTO
import no.nav.sosialhjelp.soknad.model.Botype
import no.nav.sosialhjelp.soknad.repository.BosituasjonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BosituasjonService (private val bosituasjonRepository: BosituasjonRepository) {
    @Transactional(readOnly = true)
    fun hentBosituasjon(soknadId: UUID): BosituasjonDTO? {
        // TODO skal man alltid få svar, eller exception/null hvis ikke finnes?
        return bosituasjonRepository.findById(soknadId).getOrNull()?.let {
            BosituasjonDTO(
                botype = it.botype?.name,
                antallPersoner = it.antallPersoner
            )
        }
    }

    @Transactional
    fun oppdaterBosituasjon(soknadId: UUID, dto: BosituasjonDTO) {
        bosituasjonRepository.save(
            Bosituasjon(
                soknadId = soknadId,
                botype = Botype.fromValue(dto.botype),
                antallPersoner = dto.antallPersoner
            )
        )
    }
}
