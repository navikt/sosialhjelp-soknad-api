package no.nav.sosialhjelp.soknad.v2.eier.service

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EierRegisterService(
    private val eierRepository: EierRepository,
    private val dokumentasjonService: DokumentasjonService,
) {
    @Transactional
    fun updateFromRegister(eier: Eier) {
        // oppdatere eier hvis finnes
        eierRepository
            .findByIdOrNull(eier.soknadId)
            ?.run {
                copy(
                    navn = eier.navn,
                    statsborgerskap = eier.statsborgerskap,
                    nordiskBorger = eier.nordiskBorger,
                )
            }
            ?.also { eierRepository.save(it) }
            // lagre hvis ikke finnes
            ?: eierRepository.save(eier)

        resolveOppholdstillatelse(eier)
    }

    @Transactional(readOnly = true)
    fun getKontonummer(soknadId: UUID) = eierRepository.findByIdOrNull(soknadId)?.kontonummer

    @Transactional
    fun updateKontonummerFromRegister(
        soknadId: UUID,
        kontonummerRegister: String,
    ) {
        eierRepository.findByIdOrNull(soknadId)
            ?.run {
                if (kontonummer.harIkkeKonto == true) {
                    null
                } else {
                    copy(kontonummer = kontonummer.copy(fraRegister = kontonummerRegister))
                }
            }
            ?.also { eier -> eierRepository.save(eier) }
    }

    private fun resolveOppholdstillatelse(eier: Eier) {
        if (eier.nordiskBorger == null || eier.nordiskBorger == false) {
            dokumentasjonService.opprettDokumentasjon(
                soknadId = eier.soknadId,
                opplysningType = AnnenDokumentasjonType.OPPHOLDSTILLATELSE,
            )
        }
    }
}
