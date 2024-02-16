package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class KontaktService(
    private val kontaktRepository: KontaktRepository
) {
    fun getTelefonnummer(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.telefonnummer

    fun updateTelefonnummer(soknadId: UUID, telefonnummerBruker: String): Telefonnummer {
        return getEntity(soknadId).copy(
            telefonnummer = Telefonnummer(bruker = telefonnummerBruker)
        ).let {
            kontaktRepository.save(it).telefonnummer
        }
    }

    fun getAdresser(soknadId: UUID): Adresser {
        return kontaktRepository.findByIdOrNull(soknadId)?.adresser ?: Adresser()
    }

    private fun getEntity(soknadId: UUID): Kontakt {
        return kontaktRepository.findByIdOrNull(soknadId) ?: kontaktRepository.save(Kontakt(soknadId))
    }

    fun updateBrukerAdresse(soknadId: UUID, adresseValg: AdresseValg, brukerAdresse: Adresse?): Adresser {
        return getEntity(soknadId).copy(
            adresser = getAdresser(soknadId).copy(
                brukerAdresse = brukerAdresse,
                adressevalg = adresseValg
            )
        ).let {
            kontaktRepository.save(it).adresser
        }
    }
}
