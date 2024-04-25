package no.nav.sosialhjelp.soknad.v2.eier

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger

@Service
class EierService(
    private val eierRepository: EierRepository,
) {
    private val logger by logger()

    fun getEier(soknadId: UUID) =
        eierRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Finnes ingen Eier for $soknadId. Feil")

    fun updateKontonummer(
        soknadId: UUID,
        kontonummerBruker: String? = null,
        harIkkeKonto: Boolean? = null,
    ): Kontonummer {
        return getEier(soknadId)
            .run {
                copy(
                    kontonummer = kontonummer.copy(
                        harIkkeKonto = harIkkeKonto,
                        fraBruker = kontonummerBruker)
                )
            }
            .let { eier -> eierRepository.save(eier).kontonummer }

    }

    fun updateEier(eier: Eier) = eierRepository.save(eier)

    fun updateEierFromRegister(eier: Eier) {
        eierRepository
            .findByIdOrNull(eier.soknadId)
            ?.run {
                copy(
                    navn = eier.navn,
                    statsborgerskap = eier.statsborgerskap,
                    nordiskBorger = eier.nordiskBorger,
                    kontonummer = kontonummer.copy(
                        fraRegister = eier.kontonummer.fraRegister
                    )
                )
            }
            ?.also { eierRepository.save(it) }
            ?: eierRepository.save(eier)
    }
}
