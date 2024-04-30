package no.nav.sosialhjelp.soknad.v2.kontakt.service

import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import java.util.UUID

interface AdresseService {
    fun findAdresser(soknadId: UUID): Adresser

    fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
    ): Adresser

    fun findMottaker(soknadId: UUID): NavEnhet?
}

interface TelefonService {
    fun findTelefonInfo(soknadId: UUID): Telefonnummer?

    fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer
}

interface KontaktRegisterService {
    fun saveAdresserRegister(
        soknadId: UUID,
        folkeregistrert: Adresse?,
        midlertidig: Adresse?,
    )

    fun updateTelefonRegister(
        soknadId: UUID,
        telefonRegister: String,
    )
}
