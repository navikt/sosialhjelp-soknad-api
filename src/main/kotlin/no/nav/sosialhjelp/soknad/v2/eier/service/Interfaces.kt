package no.nav.sosialhjelp.soknad.v2.eier.service

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import java.util.UUID

interface EierService {
    fun findOrError(soknadId: UUID): Eier

    fun updateKontonummer(
        soknadId: UUID,
        kontonummerBruker: String? = null,
        harIkkeKonto: Boolean? = null,
    ): Kontonummer
}

interface EierRegisterService {
    fun updateFromRegister(eier: Eier)
}
