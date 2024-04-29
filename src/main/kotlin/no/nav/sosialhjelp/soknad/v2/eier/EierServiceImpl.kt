package no.nav.sosialhjelp.soknad.v2.eier

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger

interface EierService {
    fun findEier(soknadId: UUID): Eier
    fun updateKontonummer(
        soknadId: UUID,
        kontonummerBruker: String? = null,
        harIkkeKonto: Boolean? = null,
    ): Kontonummer
}

interface RegisterDataEierService {
    fun updateEier(eier: Eier)
}

@Service
class EierServiceImpl(
    private val eierRepository: EierRepository,
): EierService, RegisterDataEierService  {
    private val logger by logger()

    override fun findEier(soknadId: UUID) =
        eierRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Finnes ingen Eier for $soknadId. Feil")

    override fun updateKontonummer(
        soknadId: UUID,
        kontonummerBruker: String?,
        harIkkeKonto: Boolean?,
    ): Kontonummer {
        logger.info("NyModell: Oppdaterer kontonummerinformasjon fra bruker")
        return findEier(soknadId)
            .run {
                copy(
                    kontonummer = kontonummer.copy(
                        harIkkeKonto = harIkkeKonto,
                        fraBruker = kontonummerBruker)
                )
            }
            .let { eier -> eierRepository.save(eier).kontonummer }
    }

    override fun updateEier(eier: Eier) {
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
