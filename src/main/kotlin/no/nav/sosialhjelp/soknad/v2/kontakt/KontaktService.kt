package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException

interface AdresseService {
    fun findAdresser(soknadId: UUID): Adresser
    fun updateBrukerAdresse(soknadId: UUID, adresseValg: AdresseValg, brukerAdresse: Adresse?): Adresser
    fun findMottaker(soknadId: UUID): NavEnhet?

}

interface TelefonService {
    fun findTelefonInfo(soknadId: UUID): Telefonnummer?
    fun updateTelefonnummer(soknadId: UUID, telefonnummerBruker: String?): Telefonnummer
}

interface KontaktRegisterService {
    fun saveAdresserRegister(soknadId: UUID, folkeregistrert: Adresse?, midlertidig: Adresse?,)
    fun updateTelefonRegister(soknadId: UUID, telefonRegister: String,)
}

@Service
class KontaktServiceImpl(
    private val kontaktRepository: KontaktRepository,
): AdresseService, TelefonService, KontaktRegisterService {
    private val logger by logger()

    override fun findTelefonInfo(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.telefonnummer

    override fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer {
        return kontaktRepository.findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer
    }

    override fun findAdresser(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.adresser
        ?: throw IkkeFunnetException("Fant ikke adresser for soknad")

    override fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
    ): Adresser {
        logger.info(
            "Oppdaterer adresse for $soknadId. " +
                    "Adressevalg: $adresseValg, " +
                    "Adresse: ${brukerAdresse?.let { "Fylt ut av bruker" }}",
        )

        return kontaktRepository.findOrCreate(soknadId)
            .run { copy(adresser = adresser.copy(valg = adresseValg, fraBruker = brukerAdresse)) }
            .let { kontaktRepository.save(it) }
            .adresser
    }

    override fun saveAdresserRegister(soknadId: UUID, folkeregistrert: Adresse?, midlertidig: Adresse?) {
        kontaktRepository
            .findOrCreate(soknadId)
            .run {
                copy(
                    adresser = adresser.copy(
                        folkeregistrert = folkeregistrert,
                        midlertidig = midlertidig,
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

    override fun findMottaker(soknadId: UUID) =  kontaktRepository.findByIdOrNull(soknadId)?.mottaker
}

private fun KontaktRepository.findOrCreate(soknadId: UUID): Kontakt {
    return findByIdOrNull(soknadId) ?: save(Kontakt(soknadId))
}
