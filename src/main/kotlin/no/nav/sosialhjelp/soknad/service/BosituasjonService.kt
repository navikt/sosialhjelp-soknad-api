package no.nav.sosialhjelp.soknad.service

import no.nav.sosialhjelp.soknad.model.soknad.Bosituasjon
import no.nav.sosialhjelp.soknad.model.BosituasjonDto
import no.nav.sosialhjelp.soknad.model.soknad.Botype
import no.nav.sosialhjelp.soknad.repository.BosituasjonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BosituasjonService (
    private val bosituasjonRepository: BosituasjonRepository
) {
    @Transactional(readOnly = true)
    fun hentBosituasjon(soknadId: UUID): BosituasjonDto? {
        // TODO skal man alltid få svar, eller exception/null hvis ikke finnes?
        return bosituasjonRepository.findById(soknadId).getOrNull()?.let {
            BosituasjonDto(
                botype = it.botype,
                antallPersoner = it.antallPersoner
            )
        }
    }

    @Transactional
    fun oppdaterBosituasjon(soknadId: UUID, bosituasjonDto: BosituasjonDto) {
        bosituasjonRepository.save(
            Bosituasjon(
                soknadId = soknadId,
                botype = Botype.fromValue(bosituasjonDto.botype?.name),
                antallPersoner = bosituasjonDto.antallPersoner
            )
        )
    }
}
