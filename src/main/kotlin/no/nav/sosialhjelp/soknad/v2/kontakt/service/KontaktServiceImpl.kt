package no.nav.sosialhjelp.soknad.v2.kontakt.service

import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface AdresseService {
    fun findAdresser(soknadId: UUID): Adresser

    fun findMottaker(soknadId: UUID): NavEnhet?

    fun updateAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
        mottaker: NavEnhet,
    )

    fun updateKommunenavnMottaker(
        soknadId: UUID,
        kommunenavn: String,
    ): String?
}

interface TelefonService {
    fun findTelefonInfo(soknadId: UUID): Telefonnummer?

    fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer
}

@Service
class KontaktServiceImpl(
    private val kontaktRepository: KontaktRepository,
) : AdresseService, TelefonService {
    @Transactional(readOnly = true)
    override fun findTelefonInfo(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.telefonnummer

    @Transactional
    override fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer =
        findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer

    @Transactional
    override fun findAdresser(soknadId: UUID) = findOrCreate(soknadId).adresser

    @Transactional
    override fun findMottaker(soknadId: UUID): NavEnhet? {
        return kontaktRepository.findByIdOrNull(soknadId)?.mottaker
    }

    @Transactional
    override fun updateAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
        mottaker: NavEnhet,
    ) {
        findOrCreate(soknadId)
            .run {
                copy(
                    adresser =
                        adresser.copy(
                            adressevalg = adresseValg,
                            fraBruker = brukerAdresse,
                        ),
                    mottaker = mottaker,
                )
            }
            .also { kontaktRepository.save(it) }
    }

    override fun updateKommunenavnMottaker(
        soknadId: UUID,
        kommunenavn: String,
    ): String {
        return kontaktRepository.findByIdOrNull(soknadId)
            ?.run { copy(mottaker = mottaker?.copy(kommunenavn = kommunenavn)) }
            ?.let { kontaktRepository.save(it) }
            ?.mottaker?.kommunenavn
            ?: error("Kunne ikke oppdatere mottakers kommunenavn")
    }

    private fun findOrCreate(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)
            ?: kontaktRepository.save(Kontakt(soknadId))
}
