package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.model.BosituasjonDTO
import no.nav.sosialhjelp.soknad.repository.BosituasjonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BosituasjonService (private val bosituasjonRepository: BosituasjonRepository) {
    @Transactional(readOnly = true)
    fun hentBosituasjon(soknadId: UUID) = bosituasjonRepository.findById(soknadId).getOrNull()

    @Transactional
    fun oppdaterBosituasjon(soknadId: UUID, dto: BosituasjonDTO) {
        bosituasjonRepository.save(
            Bosituasjon(
                soknadId = soknadId,
                botype = dto.botype,
                antallPersoner = dto.antallPersoner
            )
        )
    }
}
