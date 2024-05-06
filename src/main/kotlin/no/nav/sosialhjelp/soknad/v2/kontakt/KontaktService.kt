package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class KontaktService(
    private val kontaktRepository: KontaktRepository,
) {
    private val logger = LoggerFactory.getLogger(KontaktService::class.java)

    fun getKontaktInformasjon(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)

    fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer {
        return kontaktRepository.getOrCreateKontakt(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer
    }

    fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
    ): Kontakt {
        logger.info(
            "Oppdaterer adresse." +
                "Adressevalg: $adresseValg, " +
                "Adresse: ${brukerAdresse?.let { "Fylt ut av bruker" }}",
        )

        return kontaktRepository.getOrCreateKontakt(soknadId)
            .run { copy(adresser = adresser.copy(adressevalg = adresseValg, brukerAdresse = brukerAdresse)) }
            .let { kontaktRepository.save(it) }
    }

    fun saveAdresserRegister(
        soknadId: UUID,
        folkeregistrertAdresse: Adresse?,
        midlertidigAdresse: Adresse?,
    ) {
        logger.info(
            "Legger til adresser. " +
                "Folkeregistrert: ${folkeregistrertAdresse?.let { "Funnet" }} " +
                "Midlertidig: ${midlertidigAdresse?.let { "Funnet" }}",
        )

        kontaktRepository.getOrCreateKontakt(soknadId)
            .run {
                copy(
                    adresser =
                        adresser.copy(
                            folkeregistrertAdresse = folkeregistrertAdresse,
                            midlertidigAdresse = midlertidigAdresse,
                        ),
                )
            }
            .also { kontaktRepository.save(it) }
    }

    fun updateTelefonRegister(
        soknadId: UUID,
        telefonRegister: String,
    ) {
        kontaktRepository.getOrCreateKontakt(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraRegister = telefonRegister)) }
            .also { kontaktRepository.save(it) }
    }
}

private fun KontaktRepository.getOrCreateKontakt(soknadId: UUID): Kontakt {
    return findByIdOrNull(soknadId) ?: save(Kontakt(soknadId))
}
