package no.nav.sosialhjelp.soknad.v2.kontakt.service

import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

// TODO Denne kjører med Prop.NESTED fordi den ikke må ødelegge for annen skriving
@Transactional(propagation = Propagation.NESTED)
@Service
class KontaktRegisterService(private val kontaktRepository: KontaktRepository) {
    fun saveAdresserRegister(
        soknadId: UUID,
        folkeregistrert: Adresse?,
        midlertidig: Adresse?,
    ) {
        findOrCreate(soknadId)
            .run {
                copy(
                    adresser =
                        adresser.copy(
                            folkeregistrert = folkeregistrert,
                            midlertidig = midlertidig,
                        ),
                )
            }
            .also { kontaktRepository.save(it) }
    }

    fun updateTelefonRegister(
        soknadId: UUID,
        telefonRegister: String,
    ) {
        findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraRegister = telefonRegister)) }
            .also { kontaktRepository.save(it) }
    }

    private fun findOrCreate(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)
            ?: kontaktRepository.save(Kontakt(soknadId))
}
