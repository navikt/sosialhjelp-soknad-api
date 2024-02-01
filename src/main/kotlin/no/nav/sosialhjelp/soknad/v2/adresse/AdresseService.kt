package no.nav.sosialhjelp.soknad.v2.adresse

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdresseService(
    private val adresseRepository: AdresseRepository
) {
    @Transactional
    fun updateAdresseBruker(soknadId: UUID, brukerInputAdresse: BrukerInputAdresse): AdresserSoknad {

        val adresserSoknad = adresseRepository.findByIdOrNull(soknadId)
            ?: AdresserSoknad(soknadId = soknadId)

        adresserSoknad.brukerInput = brukerInputAdresse

        return adresseRepository.save(adresserSoknad)
    }

    @Transactional(readOnly = true)
    fun getAdresserSoknad(soknadId: UUID): AdresserSoknad {
        return adresseRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Adresser for soknad ${soknadId} ikke funnet")
    }
}
