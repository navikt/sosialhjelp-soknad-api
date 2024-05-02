package no.nav.sosialhjelp.soknad.v2.kontakt.service

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class KontaktServiceImpl(
    private val kontaktRepository: KontaktRepository,
) : AdresseService, TelefonService, KontaktRegisterService {
    private val logger by logger()

    override fun findTelefonInfo(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.telefonnummer

    override fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer {
        return findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer
    }

    override fun findAdresser(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)?.adresser
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

        return findOrCreate(soknadId)
            .run { copy(adresser = adresser.copy(adressevalg = adresseValg, fraBruker = brukerAdresse)) }
            .let { kontaktRepository.save(it) }
            .adresser
    }

    override fun saveAdresserRegister(
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

    override fun updateTelefonRegister(
        soknadId: UUID,
        telefonRegister: String,
    ) {
        findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraRegister = telefonRegister)) }
            .also { kontaktRepository.save(it) }
    }

    override fun findMottaker(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.mottaker

    private fun findOrCreate(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)
            ?: kontaktRepository.save(Kontakt(soknadId))
}
