package no.nav.sosialhjelp.soknad.v2.eier.service

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

// TODO Denne kjører med Prop.NESTED fordi den ikke må ødelegge for annen skriving
@Transactional(propagation = Propagation.NESTED)
@Service
class EierRegisterService(private val eierRepository: EierRepository) {
    fun updateFromRegister(eier: Eier) {
        // oppdatere eier hvis finnes
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

    fun updateKontonummerFraRegister(
        soknadId: String,
        kontonummerRegister: String,
    ) {
        eierRepository
            .findByIdOrNull(UUID.fromString(soknadId))
            ?.run { copy(kontonummer = kontonummer.copy(fraRegister = kontonummerRegister)) }
            ?.also { eierRepository.save(it) }
    }
}
