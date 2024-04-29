package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger


interface KontaktService {
    fun getKontaktInformasjon(soknadId: UUID): Kontakt?
    fun updateTelefonnummer(soknadId: UUID, telefonnummerBruker: String?): Telefonnummer
    fun updateBrukerAdresse(soknadId: UUID, adresseValg: AdresseValg, brukerAdresse: Adresse?): Kontakt
}

interface RegisterDataKontaktService {
    fun saveAdresserRegister(soknadId: UUID, folkeregistrert: Adresse?, midlertidig: Adresse?,)
    fun updateTelefonRegister(soknadId: UUID, telefonRegister: String,)
}

@Service
class KontaktServiceImpl(
    private val kontaktRepository: KontaktRepository,
): KontaktService, RegisterDataKontaktService {
    private val logger by logger()

    override fun getKontaktInformasjon(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)

    override fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer {
        return kontaktRepository.findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer
    }

    override fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
    ): Kontakt {
        logger.info(
            "Oppdaterer adresse for $soknadId. " +
                    "Adressevalg: $adresseValg, " +
                    "Adresse: ${brukerAdresse?.let { "Fylt ut av bruker" }}",
        )

        return kontaktRepository.findOrCreate(soknadId)
            .run { copy(adresser = adresser.copy(adressevalg = adresseValg, brukerAdresse = brukerAdresse)) }
            .let { kontaktRepository.save(it) }
    }

    override fun saveAdresserRegister(soknadId: UUID, folkeregistrert: Adresse?, midlertidig: Adresse?) {
        kontaktRepository
            .findOrCreate(soknadId)
            .run {
                copy(
                    adresser = adresser.copy(
                        folkeregistrertAdresse = folkeregistrert,
                        midlertidigAdresse = midlertidig,
                    ),
                )
            }
            .also { kontaktRepository.save(it) }
    }

    override fun updateTelefonRegister(soknadId: UUID, telefonRegister: String,) {
        kontaktRepository
            .findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraRegister = telefonRegister)) }
            .also { kontaktRepository.save(it) }
    }
}

private fun KontaktRepository.findOrCreate(soknadId: UUID): Kontakt {
    return findByIdOrNull(soknadId) ?: save(Kontakt(soknadId))
}
