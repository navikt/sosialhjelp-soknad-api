package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import java.time.LocalDateTime

interface SoknadMetadataRepository {
    fun hentNesteId(): Long?
    fun opprett(metadata: SoknadMetadata)
    fun oppdater(metadata: SoknadMetadata?)
    fun hent(behandlingsId: String?): SoknadMetadata?
    fun hentBehandlingskjede(behandlingsId: String?): List<SoknadMetadata>
    fun hentAntallInnsendteSoknaderEtterTidspunkt(fnr: String?, tidspunkt: LocalDateTime?): Int?
    fun hentSvarUtInnsendteSoknaderForBruker(fnr: String): List<SoknadMetadata>
    fun hentAlleInnsendteSoknaderForBruker(fnr: String): List<SoknadMetadata>
    fun hentPabegynteSoknaderForBruker(fnr: String): List<SoknadMetadata>
    fun hentPabegynteSoknaderForBruker(fnr: String, lestDittNav: Boolean): List<SoknadMetadata>
    fun hentInnsendteSoknaderForBrukerEtterTidspunkt(fnr: String, tidsgrense: LocalDateTime): List<SoknadMetadata>
    fun oppdaterLestDittNav(soknadMetadata: SoknadMetadata, fnr: String)
}
