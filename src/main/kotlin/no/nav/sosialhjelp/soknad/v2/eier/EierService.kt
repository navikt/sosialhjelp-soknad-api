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

        return getEier(soknadId)
            .run {
                val kontonummer = this.kontonummer ?: Kontonummer()
                copy(kontonummer = kontonummer.copy(harIkkeKonto = harIkkeKonto, fraBruker = kontonummerBruker))
            }
            .let { eier -> eierRepository.save(eier) }
            .kontonummer!!
    }
}
