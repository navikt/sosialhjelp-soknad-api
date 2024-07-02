package no.nav.sosialhjelp.soknad.v2.situasjonsendring.service

import no.nav.sosialhjelp.soknad.v2.situasjonsendring.Situasjonsendring
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringRepository
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringService
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class SituasjonsendringServiceImpl(
    private val situasjonsendringRepository: SituasjonsendringRepository,
) : SituasjonsendringService {
    override fun updateSituasjonsendring(
        soknadId: UUID,
        hvaErEndret: String?,
        endring: Boolean?,
    ): Situasjonsendring {
        val situasjonsendring = situasjonsendringRepository.findById(soknadId).getOrNull() ?: Situasjonsendring(soknadId, hvaErEndret, endring)
        val oppdatert = situasjonsendring.copy(hvaErEndret = hvaErEndret, endring = endring)
        return situasjonsendringRepository.save(oppdatert)
    }

    override fun getSituasjonsendring(soknadId: UUID): Situasjonsendring? = situasjonsendringRepository.findById(soknadId).getOrNull()
}
