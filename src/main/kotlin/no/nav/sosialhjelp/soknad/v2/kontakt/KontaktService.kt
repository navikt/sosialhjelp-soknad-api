package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class KontaktService(
    private val kontaktRepository: KontaktRepository
) {
    fun getKontaktInformasjon(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)

    fun updateTelefonnummer(soknadId: UUID, telefonnummerBruker: String?): Telefonnummer {
        return getOrCreateKontakt(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer
    }

    fun updateBrukerAdresse(soknadId: UUID, adresseValg: AdresseValg, brukerAdresse: Adresse?): Kontakt {
        return getOrCreateKontakt(soknadId)
            .run { copy(adresser = adresser.copy(adressevalg = adresseValg, brukerAdresse = brukerAdresse)) }
            .let { kontaktRepository.save(it) }
    }

    private fun getOrCreateKontakt(soknadId: UUID): Kontakt {
        return kontaktRepository.findByIdOrNull(soknadId) ?: kontaktRepository.save(Kontakt(soknadId))
    }
}
