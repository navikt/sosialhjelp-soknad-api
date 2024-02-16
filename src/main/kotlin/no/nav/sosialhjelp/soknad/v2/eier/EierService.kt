package no.nav.sosialhjelp.soknad.v2.eier

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class EierService(
    private val eierRepository: EierRepository
) {
    fun getEier(soknadId: UUID) = eierRepository.findByIdOrNull(soknadId)
        ?: throw IkkeFunnetException("Finnes ingen Eier for $soknadId. Feil")

    fun updateKontonummer(
        soknadId: UUID,
        kontonummerBruker: String? = null,
        harIkkeKonto: Boolean? = null
    ): Kontonummer {

        val eier = eierRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Eier finnes ikke")

        return eier.kontonummer.copy(
            harIkkeKonto = harIkkeKonto,
            bruker = kontonummerBruker
        ).also {
            eierRepository
                .save(eier.copy(kontonummer = it))
        }
    }
}
