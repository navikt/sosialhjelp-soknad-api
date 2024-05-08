package no.nav.sosialhjelp.soknad.v2.eier.service

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface EierService {
    fun findOrError(soknadId: UUID): Eier

    fun updateKontonummer(
        soknadId: UUID,
        kontonummerBruker: String? = null,
        harIkkeKonto: Boolean? = null,
    ): Kontonummer
}

@Service
class EierServiceImpl(
    private val eierRepository: EierRepository,
) : EierService {
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

    companion object {
        private val logger by logger()
    }
}
