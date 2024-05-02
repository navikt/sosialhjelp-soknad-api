package no.nav.sosialhjelp.soknad.v2.eier.service

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class EierServiceImpl(
    private val eierRepository: EierRepository,
) : EierService, EierRegisterService {
    override fun findOrError(soknadId: UUID) =
        eierRepository.findByIdOrNull(soknadId)
            ?: throw IkkeFunnetException("Finnes ingen Eier. Feil")

    override fun updateKontonummer(
        soknadId: UUID,
        kontonummerBruker: String?,
        harIkkeKonto: Boolean?,
    ): Kontonummer {
        logger.info("NyModell: Oppdaterer kontonummerinformasjon fra bruker")
        return findOrError(soknadId)
            .run {
                copy(
                    kontonummer =
                        kontonummer.copy(
                            harIkkeKonto = harIkkeKonto,
                            fraBruker = kontonummerBruker,
                        ),
                )
            }
            .let { eier -> eierRepository.save(eier).kontonummer }
    }

    override fun updateFromRegister(eier: Eier) {
        //oppdatere eier hvis finnes
        eierRepository
            .findByIdOrNull(eier.soknadId)
            ?.run {
                copy(
                    navn = eier.navn,
                    statsborgerskap = eier.statsborgerskap,
                    nordiskBorger = eier.nordiskBorger,
                    kontonummer =
                        kontonummer.copy(
                            fraRegister = eier.kontonummer.fraRegister,
                        ),
                )
            }
            ?.also { eierRepository.save(it) }
            // lagre hvis ikke finnes
            ?: eierRepository.save(eier)
    }

    companion object {
        private val logger by logger()
    }
}
