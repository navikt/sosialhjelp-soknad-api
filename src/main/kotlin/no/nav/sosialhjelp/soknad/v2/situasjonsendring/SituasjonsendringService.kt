package no.nav.sosialhjelp.soknad.v2.situasjonsendring

import java.util.UUID

interface SituasjonsendringService {
    fun updateSituasjonsendring(
        soknadId: UUID,
        hvaErEndret: String?,
        endring: Boolean?,
    ): Situasjonsendring

    fun getSituasjonsendring(
        soknadId: UUID,
    ): Situasjonsendring?

    fun deleteSituasjonsendring(soknadId: UUID)
}
